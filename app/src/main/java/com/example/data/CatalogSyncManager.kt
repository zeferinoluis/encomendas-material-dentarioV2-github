package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object CatalogSyncManager {

    /**
     * Converts common Google Drive / Sheets web sharing links to direct CSV download links.
     */
    fun sanitizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return when {
            // Google Sheets edit/pub link
            trimmed.contains("docs.google.com/spreadsheets/d/") -> {
                val sheetId = trimmed.substringAfter("docs.google.com/spreadsheets/d/")
                    .substringBefore("/")
                    .substringBefore("?")
                    .substringBefore("#")
                "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"
            }
            // Google Drive file link
            trimmed.contains("drive.google.com/file/d/") -> {
                val fileId = trimmed.substringAfter("drive.google.com/file/d/")
                    .substringBefore("/")
                    .substringBefore("?")
                    .substringBefore("#")
                "https://drive.google.com/uc?export=download&id=$fileId"
            }
            else -> trimmed
        }
    }

    /**
     * Splits a CSV line by delimiter while honoring double quotes.
     */
    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Parses CSV stream (semicolon or comma separated) into a list of Products.
     */
    fun parseCsvStream(inputStream: InputStream): List<Product> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val products = mutableListOf<Product>()

        val rawHeader = reader.readLine() ?: return emptyList()
        val headerLine = rawHeader.removePrefix("\uFEFF").trim()
        val delimiterChar = if (headerLine.contains(";")) ';' else ','

        var line: String? = reader.readLine()
        while (line != null) {
            val cleanLine = line.removePrefix("\uFEFF").trim()
            if (cleanLine.isNotBlank()) {
                val tokens = splitCsvLine(cleanLine, delimiterChar)
                if (tokens.size >= 2) {
                    val p = parseTokensToProduct(tokens)
                    if (p != null && p.code.isNotBlank()) {
                        products.add(p)
                    }
                }
            }
            line = reader.readLine()
        }
        return products
    }

    private fun parseTokensToProduct(tokens: List<String>): Product? {
        // Expected columns: id;referencia;nome_produto;marca;categoria;especificacao;caracteristicas;preco_eur;preco_formatado;pagina_catalogo
        // Fallbacks for simpler 3-4 column CSVs: code;company;description;price
        return try {
            if (tokens.size >= 8) {
                val ref = tokens.getOrNull(1)?.trim().orEmpty()
                val id = tokens.getOrNull(0)?.trim().orEmpty()
                val code = if (ref.isNotBlank()) ref else id

                val name = tokens.getOrNull(2)?.trim().orEmpty()
                val marca = tokens.getOrNull(3)?.trim().orEmpty()
                val category = tokens.getOrNull(4)?.trim().orEmpty()
                val espec = tokens.getOrNull(5)?.trim().orEmpty()
                val carac = tokens.getOrNull(6)?.trim().orEmpty()
                val priceEurRaw = tokens.getOrNull(7)?.trim().orEmpty().replace(",", ".")
                val priceFmtRaw = tokens.getOrNull(8)?.trim().orEmpty()

                val priceValue = priceEurRaw.toDoubleOrNull() ?: 0.0
                val priceFormatted = when {
                    priceFmtRaw.isNotBlank() && !priceFmtRaw.equals("Sob consulta", ignoreCase = true) -> priceFmtRaw
                    priceValue > 0 -> String.format(Locale.US, "%.2f €", priceValue)
                    else -> "Sob consulta"
                }

                // Build rich description
                val descParts = mutableListOf<String>()
                if (name.isNotBlank() && !name.equals("Produto Dentário", ignoreCase = true)) {
                    descParts.add(name)
                }
                if (espec.isNotBlank()) descParts.add(espec)
                if (carac.isNotBlank()) descParts.add(carac)
                if (category.isNotBlank() && !descParts.any { it.contains(category, ignoreCase = true) }) {
                    descParts.add("($category)")
                }

                val finalDesc = if (descParts.isNotEmpty()) descParts.joinToString(" - ") else name.ifBlank { "Produto $code" }
                val finalCompany = if (marca.isNotBlank() && marca != "-") marca else "Montellano"

                Product(
                    code = code,
                    company = finalCompany,
                    description = finalDesc,
                    priceRange = priceFormatted,
                    minPrice = priceValue,
                    maxPrice = priceValue,
                    invoicesCount = 1,
                    category = category
                )
            } else if (tokens.size >= 3) {
                // Simple format: code;company;description;price
                val code = tokens[0].trim()
                val company = tokens.getOrNull(1)?.trim().orEmpty().ifBlank { "Montellano" }
                val desc = tokens.getOrNull(2)?.trim().orEmpty().ifBlank { "Produto $code" }
                val priceRaw = tokens.getOrNull(3)?.trim().orEmpty().replace(",", ".")
                val valDouble = priceRaw.toDoubleOrNull() ?: 0.0
                val priceFmt = if (valDouble > 0) String.format(Locale.US, "%.2f €", valDouble) else "Sob consulta"

                Product(
                    code = code,
                    company = company,
                    description = desc,
                    priceRange = priceFmt,
                    minPrice = valDouble,
                    maxPrice = valDouble,
                    invoicesCount = 1
                )
            } else null
        } catch (e: Exception) {
            Log.e("CatalogSyncManager", "Error parsing CSV row: $tokens", e)
            null
        }
    }

    /**
     * Downloads CSV from the given URL.
     */
    suspend fun downloadCsvFromUrl(rawUrl: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        val targetUrl = sanitizeUrl(rawUrl)
        var connection: HttpURLConnection? = null
        try {
            // Loop manual para seguir redirecionamentos entre protocolos (HTTP -> HTTPS),
            // que o HttpURLConnection não segue automaticamente mesmo com
            // instanceFollowRedirects = true.
            var currentUrl = targetUrl
            var redirectCount = 0
            val maxRedirects = 5
            var responseCode: Int

            do {
                connection?.disconnect()
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")

                responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val newUrl = connection.getHeaderField("Location")
                    if (newUrl.isNullOrBlank()) break
                    currentUrl = newUrl
                    redirectCount++
                } else {
                    break
                }
            } while (redirectCount < maxRedirects)

            if (responseCode in 200..299) {
                val stream = connection!!.inputStream
                val products = parseCsvStream(stream)
                if (products.isNotEmpty()) {
                    Result.success(products)
                } else {
                    Result.failure(Exception("O ficheiro CSV obtido está vazio ou em formato inválido."))
                }
            } else {
                Result.failure(Exception("Erro ao aceder ao servidor (Código HTTP: $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de ligação: ${e.localizedMessage ?: "Verifique a ligação à internet ou o link público."}"))
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Loads catalog from local assets/catalog.csv if available.
     */
    fun loadFromAssets(context: Context): List<Product> {
        return try {
            context.assets.open("catalog.csv").use { stream ->
                parseCsvStream(stream)
            }
        } catch (e: Exception) {
            Log.e("CatalogSyncManager", "Could not load catalog.csv from assets", e)
            emptyList()
        }
    }
}
