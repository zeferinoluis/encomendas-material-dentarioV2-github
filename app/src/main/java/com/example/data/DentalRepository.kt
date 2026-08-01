package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Locale

class DentalRepository(
    private val productDao: ProductDao,
    private val savedOrderDao: SavedOrderDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val savedOrders: Flow<List<SavedOrder>> = savedOrderDao.getAllSavedOrders()

    suspend fun checkAndInitializeCatalog(context: android.content.Context) {
        val count = productDao.getProductCount()
        val assetProducts = CatalogSyncManager.loadFromAssets(context)
        val combinedProducts = (assetProducts + CatalogData.initialProducts)
            .distinctBy { it.code.lowercase() }
        if (count < combinedProducts.size) {
            syncCatalogFromList(combinedProducts)
        }
    }

    suspend fun syncCatalogFromList(newProducts: List<Product>): Int {
        val currentList = productDao.getCurrentProductsList()
        val currentStates = currentList.associateBy(
            keySelector = { it.code.lowercase() },
            valueTransform = { Triple(it.qty, it.isSelected, it.isFavorite) }
        )

        val mergedProducts = newProducts.map { product ->
            val state = currentStates[product.code.lowercase()]
            if (state != null) {
                product.copy(
                    qty = state.first,
                    isSelected = state.second,
                    isFavorite = state.third
                )
            } else {
                product
            }
        }

        productDao.insertAll(mergedProducts)
        return mergedProducts.size
    }

    suspend fun syncCatalogFromUrl(url: String): Result<Int> {
        val result = CatalogSyncManager.downloadCsvFromUrl(url)
        return if (result.isSuccess) {
            val products = result.getOrNull().orEmpty()
            val count = syncCatalogFromList(products)
            Result.success(count)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Erro ao descarregar catálogo."))
        }
    }

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updatePrice(code: String, newPrice: Double) {
        val formattedPrice = String.format(Locale.US, "%.2f €", newPrice)
        productDao.updateProductPrice(code, formattedPrice, newPrice, newPrice)
    }

    suspend fun updateQuantity(code: String, qty: Int) {
        val safeQty = qty.coerceAtLeast(0)
        val isSelected = safeQty > 0
        productDao.updateProductQuantity(code, safeQty, isSelected)
    }

    suspend fun toggleSelected(code: String, selected: Boolean) {
        val qty = if (selected) 1 else 0
        productDao.updateProductQuantity(code, qty, selected)
    }

    suspend fun setBatchQuantity(codes: List<String>, qty: Int) {
        val safeQty = qty.coerceAtLeast(0)
        val isSelected = safeQty > 0
        if (codes.isNotEmpty()) {
            productDao.updateBatchQuantity(codes, safeQty, isSelected)
        }
    }

    suspend fun toggleFavorite(code: String, isFavorite: Boolean) {
        productDao.updateProductFavorite(code, isFavorite)
    }

    suspend fun clearAllQuantities() {
        productDao.clearAllQuantities()
    }

    suspend fun saveOrder(order: SavedOrder) {
        savedOrderDao.insertOrder(order)
    }

    suspend fun deleteSavedOrder(id: Long) {
        savedOrderDao.deleteOrderById(id)
    }
}
