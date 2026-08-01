package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CategoryStats
import com.example.data.CategoryUtils
import com.example.data.DentalRepository
import com.example.data.Product
import com.example.data.SavedOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CompanyFilter(val label: String) {
    ALL("Todos"),
    MONTELLANO("Montellano"),
    ASSISDENT("Assisdent"),
    FAVORITES("Favoritos"),
    SELECTED_ONLY("Selecionados")
}

enum class SortOption(val label: String) {
    DEFAULT("Padrão"),
    NAME_ASC("Nome A-Z"),
    PRICE_ASC("Preço: Menor"),
    PRICE_DESC("Preço: Maior"),
    CODE("Código")
}

data class CartSummary(
    val selectedCount: Int = 0,
    val totalUnits: Int = 0,
    val totalPrice: Double = 0.0
)

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val count: Int, val isAutoSync: Boolean = false) : SyncState()
    data class Error(val message: String) : SyncState()
}

class DentalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DentalRepository

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(CompanyFilter.ALL)
    val selectedSortOption = MutableStateFlow(SortOption.DEFAULT)
    val selectedCategory = MutableStateFlow<String?>(null)

    val syncStatus = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncUrl = MutableStateFlow("")

    val products: StateFlow<List<Product>>
    val filteredProducts: StateFlow<List<Product>>
    val categoryStats: StateFlow<List<CategoryStats>>
    val cartSummary: StateFlow<CartSummary>
    val savedOrders: StateFlow<List<SavedOrder>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DentalRepository(db.productDao(), db.savedOrderDao())

        val prefs = application.getSharedPreferences("dental_app_prefs", android.content.Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("sync_url", "") ?: ""
        syncUrl.value = savedUrl

        viewModelScope.launch {
            repository.checkAndInitializeCatalog(application)
            if (savedUrl.isNotBlank()) {
                syncCatalog(savedUrl, isAutoSync = true)
            }
        }

        products = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedOrders = repository.savedOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        categoryStats = products.map { list ->
            val grouped = list.groupBy { CategoryUtils.getNormalizedCategory(it) }
            CategoryUtils.CATEGORIES.map { catInfo ->
                val catProducts = grouped[catInfo.name].orEmpty()
                val selectedCount = catProducts.count { it.qty > 0 || it.isSelected }
                val totalQty = catProducts.sumOf { it.qty }
                CategoryStats(
                    categoryName = catInfo.name,
                    categoryInfo = catInfo,
                    totalProducts = catProducts.size,
                    selectedProductsCount = selectedCount,
                    totalQuantity = totalQty
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredProducts = combine(products, searchQuery, selectedFilter, selectedSortOption, selectedCategory) { list, query, filter, sort, category ->
            val queryClean = query.trim().lowercase()
            val filtered = list.filter { product ->
                val prodCategory = CategoryUtils.getNormalizedCategory(product)
                val matchesCategory = if (category == null) true else prodCategory.equals(category, ignoreCase = true)

                val matchesFilter = when (filter) {
                    CompanyFilter.ALL -> true
                    CompanyFilter.MONTELLANO -> product.company.equals("Montellano", ignoreCase = true)
                    CompanyFilter.ASSISDENT -> product.company.equals("Assisdent", ignoreCase = true)
                    CompanyFilter.FAVORITES -> product.isFavorite
                    CompanyFilter.SELECTED_ONLY -> product.qty > 0 || product.isSelected
                }

                val matchesSearch = if (queryClean.isEmpty()) {
                    true
                } else {
                    val pCode = product.code.lowercase()
                    val pDesc = product.description.lowercase()
                    val pComp = product.company.lowercase()

                    pCode.contains(queryClean) ||
                    pDesc.contains(queryClean) ||
                    pComp.contains(queryClean) ||
                    prodCategory.lowercase().contains(queryClean) ||
                    queryClean.contains(pCode) ||
                    // Match parenthesized REF in description e.g. "(25950)"
                    Regex("""\(([a-zA-Z0-9\-\.\/]+)\)""")
                        .findAll(product.description)
                        .map { it.groupValues[1].lowercase() }
                        .any { ref -> ref.length >= 3 && (queryClean.contains(ref) || ref.contains(queryClean)) } ||
                    // HIBC primary segment matching
                    if (queryClean.contains("+")) {
                        val hibcPrimary = queryClean.substringAfter("+").substringBefore("/").substringBefore("*").trim()
                        hibcPrimary.isNotBlank() && (
                            pCode.contains(hibcPrimary) ||
                            pDesc.contains(hibcPrimary) ||
                            hibcPrimary.contains(pCode)
                        )
                    } else false
                }

                matchesCategory && matchesFilter && matchesSearch
            }

            when (sort) {
                SortOption.DEFAULT -> filtered
                SortOption.NAME_ASC -> filtered.sortedBy { it.description }
                SortOption.PRICE_ASC -> filtered.sortedBy { it.minPrice }
                SortOption.PRICE_DESC -> filtered.sortedByDescending { it.minPrice }
                SortOption.CODE -> filtered.sortedBy { it.code }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        cartSummary = products.combine(MutableStateFlow(Unit)) { list, _ ->
            var selectedCount = 0
            var totalUnits = 0
            var totalPrice = 0.0

            list.forEach { item ->
                if (item.qty > 0 || item.isSelected) {
                    selectedCount++
                    totalUnits += item.qty
                    totalPrice += item.qty * item.minPrice
                }
            }

            CartSummary(
                selectedCount = selectedCount,
                totalUnits = totalUnits,
                totalPrice = totalPrice
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartSummary()
        )
    }

    fun addNewProduct(
        code: String,
        company: String,
        description: String,
        priceRange: String,
        priceValue: Double
    ) {
        viewModelScope.launch {
            val newProd = Product(
                code = code.trim(),
                company = company.trim(),
                description = description.trim(),
                priceRange = if (priceRange.isNotBlank()) priceRange.trim() else String.format("%.2f €", priceValue),
                minPrice = priceValue,
                maxPrice = priceValue,
                invoicesCount = 1,
                qty = 0,
                isSelected = false
            )
            repository.addProduct(newProd)
        }
    }

    fun updatePrice(code: String, newPrice: Double) {
        viewModelScope.launch {
            repository.updatePrice(code, newPrice)
        }
    }

    fun updateQuantity(code: String, qty: Int) {
        viewModelScope.launch {
            repository.updateQuantity(code, qty)
        }
    }

    fun toggleSelection(code: String, selected: Boolean) {
        viewModelScope.launch {
            repository.toggleSelected(code, selected)
        }
    }

    fun toggleSelectAllVisible(selectAll: Boolean) {
        viewModelScope.launch {
            val visibleCodes = filteredProducts.value.map { it.code }
            val qty = if (selectAll) 1 else 0
            repository.setBatchQuantity(visibleCodes, qty)
        }
    }

    fun clearOrder() {
        viewModelScope.launch {
            repository.clearAllQuantities()
        }
    }

    fun saveCurrentOrder(notes: String = "") {
        viewModelScope.launch {
            val selectedItems = products.value.filter { it.qty > 0 }
            if (selectedItems.isEmpty()) return@launch

            val summaryLines = selectedItems.joinToString("\n") {
                "[${it.company}] ${it.code} - ${it.description} x${it.qty} (${String.format("%.2f", it.qty * it.minPrice)} €)"
            }

            val summary = cartSummary.value
            val order = SavedOrder(
                itemCount = summary.selectedCount,
                totalUnits = summary.totalUnits,
                totalPrice = summary.totalPrice,
                itemsSummary = summaryLines,
                notes = notes
            )
            repository.saveOrder(order)
        }
    }

    fun toggleFavorite(code: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(code, isFavorite)
        }
    }

    fun restoreSavedOrder(itemsSummary: String) {
        viewModelScope.launch {
            val lines = itemsSummary.lines()
            val allProds = products.value
            lines.forEach { line ->
                if (line.isBlank()) return@forEach

                val qtyRegex = Regex("""(?:x\s*|Qtd:\s*)(\d+)""", RegexOption.IGNORE_CASE)
                val qtyMatch = qtyRegex.find(line)
                val qty = qtyMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

                val matchingProd = allProds.firstOrNull { prod ->
                    line.contains("[${prod.company}]") && (
                        line.contains(" ${prod.code} ") ||
                        line.contains("(${prod.code})") ||
                        line.contains(" ${prod.code}-") ||
                        line.contains("${prod.code} -")
                    )
                } ?: allProds.firstOrNull { prod ->
                    line.contains(prod.code)
                }

                if (matchingProd != null) {
                    repository.updateQuantity(matchingProd.code, qty)
                }
            }
        }
    }

    fun deleteSavedOrder(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedOrder(id)
        }
    }

    fun selectCategory(categoryName: String?) {
        selectedCategory.value = categoryName
    }

    fun saveSyncUrl(url: String) {
        val trimmed = url.trim()
        syncUrl.value = trimmed
        val prefs = getApplication<Application>().getSharedPreferences("dental_app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("sync_url", trimmed).apply()
    }

    fun resetSyncStatus() {
        syncStatus.value = SyncState.Idle
    }

    fun syncCatalog(url: String, isAutoSync: Boolean = false) {
        val targetUrl = url.trim().ifBlank { syncUrl.value.trim() }
        if (targetUrl.isBlank()) {
            syncStatus.value = SyncState.Error("Introduza um link do Google Sheets / Drive em CSV.")
            return
        }

        saveSyncUrl(targetUrl)
        syncStatus.value = SyncState.Loading

        viewModelScope.launch {
            val result = repository.syncCatalogFromUrl(targetUrl)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                syncStatus.value = SyncState.Success(count, isAutoSync)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erro de sincronização."
                syncStatus.value = SyncState.Error(err)
            }
        }
    }
}
