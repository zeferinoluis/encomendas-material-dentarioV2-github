package com.example.data

import java.util.Locale

data class CategoryInfo(
    val name: String,
    val emoji: String,
    val description: String,
    val colorHex: Long = 0xFF2563EB
)

data class CategoryStats(
    val categoryName: String,
    val categoryInfo: CategoryInfo,
    val totalProducts: Int,
    val selectedProductsCount: Int,
    val totalQuantity: Int
)

object CategoryUtils {

    val CATEGORIES = listOf(
        CategoryInfo(
            name = "Agulhas & Anestesia",
            emoji = "💉",
            description = "Agulhas, anestésicos, seringas de aspiração e carules",
            colorHex = 0xFF0284C7 // Sky Blue
        ),
        CategoryInfo(
            name = "Desinfecção & Esterilização",
            emoji = "🧼",
            description = "Desinfectantes de superfícies, aspiração, rolos e testes",
            colorHex = 0xFF0D9488 // Teal
        ),
        CategoryInfo(
            name = "Dentística & Restauração",
            emoji = "🦷",
            description = "Compósitos, adesivos, matrizes, tiras de lixa e gravadores",
            colorHex = 0xFF2563EB // Royal Blue
        ),
        CategoryInfo(
            name = "Impressão & Prótese",
            emoji = "🧪",
            description = "Silicones, alginatos, gessos, ceras e moldeiras",
            colorHex = 0xFF7C3AED // Purple
        ),
        CategoryInfo(
            name = "Rotativos & Brocas",
            emoji = "⚡",
            description = "Brocas de tungsténio, diamantes, turbinas e sprays",
            colorHex = 0xFFD97706 // Amber/Orange
        ),
        CategoryInfo(
            name = "Endodontia & Cirurgia",
            emoji = "🩸",
            description = "Limas ProTaper, suturas de seda/silkam, alavancas e bisturis",
            colorHex = 0xFFDC2626 // Red
        ),
        CategoryInfo(
            name = "Descartáveis & Proteção",
            emoji = "🛡️",
            description = "Babetes, copos, luvas, aspiradores, algodão e campos",
            colorHex = 0xFF059669 // Emerald Green
        ),
        CategoryInfo(
            name = "Higiene & Branqueamento",
            emoji = "✨",
            description = "Pasta profilaxia, escovas nylon, Pola Night e kits",
            colorHex = 0xFFDB2777 // Pink
        ),
        CategoryInfo(
            name = "Outros Artigos",
            emoji = "📦",
            description = "Diversos materiais e acessórios médico-dentários",
            colorHex = 0xFF475569 // Slate
        )
    )

    private val categoryMap = CATEGORIES.associateBy { it.name }

    fun getCategoryInfo(categoryName: String): CategoryInfo {
        return categoryMap[categoryName] ?: CategoryInfo(
            name = categoryName,
            emoji = "🏷️",
            description = "Artigos da categoria $categoryName",
            colorHex = 0xFF475569
        )
    }

    /**
     * Determines the clean Portuguese category for a product based on explicit field or text matching.
     */
    fun getNormalizedCategory(product: Product): String {
        if (product.category.isNotBlank()) {
            val mapped = mapRawCategory(product.category)
            if (mapped != "Outros Artigos") return mapped
        }

        val text = "${product.description} ${product.company}".lowercase(Locale.ROOT)

        return when {
            text.containsAny("agulha", "anestes", "articaina", "lidocaina", "xilonibsa", "topigel", "normonjet", "seringa", "infiltra", "troncular") -> "Agulhas & Anestesia"
            text.containsAny("desinf", "esterili", "cavicide", "puli-jet", "helix", "sterigut", "toalhete", "autoclave", "detergente", "limpeza") -> "Desinfecção & Esterilização"
            text.containsAny("composito", "adesivo", "etch", "bulk fill", "matriz", "lixa", "polim", "maxcem", "restaur", "resina", "cera modelar", "match shade") -> "Dentística & Restauração"
            text.containsAny("silicone", "alginato", "hydrogum", "occlufast", "putty", "impress", "molde", "gesso", "caixa aparelho") -> "Impressão & Prótese"
            text.containsAny("broca", "diamante", "diam komet", "turbina", "nsk", "pana plus", "rotativo", "contra-angulo", "peca de mao", "spray lubrif") -> "Rotativos & Brocas"
            text.containsAny("protaper", "lima", "sutura", "silkam", "seda", "alavanca", "bisturi", "cirurg", "endodon", "gutapercha", "gelo") -> "Endodontia & Cirurgia"
            text.containsAny("babete", "copo", "luva", "aspirador", "algodao", "protector rvg", "mascara", "campo", "dodeira", "dedeira", "descart") -> "Descartáveis & Proteção"
            text.containsAny("pola night", "bleaching", "profilax", "escova nylon", "whitesmile", "branquea", "higiene") -> "Higiene & Branqueamento"
            else -> "Outros Artigos"
        }
    }

    private fun mapRawCategory(raw: String): String {
        val lower = raw.trim().lowercase(Locale.ROOT)
        return when {
            lower.containsAny("agujas", "anestesia", "agulha") -> "Agulhas & Anestesia"
            lower.containsAny("desinfe", "esterili") -> "Desinfecção & Esterilização"
            lower.containsAny("compuesto", "adhesivo", "restaura", "composito") -> "Dentística & Restauração"
            lower.containsAny("impresion", "silicon", "alginat", "protesis", "impressao") -> "Impressão & Prótese"
            lower.containsAny("rotatorio", "fresa", "broca", "turbin") -> "Rotativos & Brocas"
            lower.containsAny("endodon", "cirugia", "sutura", "lima") -> "Endodontia & Cirurgia"
            lower.containsAny("desechable", "proteccion", "descart") -> "Descartáveis & Proteção"
            lower.containsAny("blanqueamiento", "profilaxis", "higiene") -> "Higiene & Branqueamento"
            else -> "Outros Artigos"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
