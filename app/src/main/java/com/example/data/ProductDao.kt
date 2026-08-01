package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    suspend fun getCurrentProductsList(): List<Product>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("UPDATE products SET qty = :qty, isSelected = :isSelected WHERE code = :code")
    suspend fun updateProductQuantity(code: String, qty: Int, isSelected: Boolean)

    @Query("UPDATE products SET priceRange = :priceRange, minPrice = :minPrice, maxPrice = :maxPrice WHERE code = :code")
    suspend fun updateProductPrice(code: String, priceRange: String, minPrice: Double, maxPrice: Double)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE code = :code")
    suspend fun updateProductFavorite(code: String, isFavorite: Boolean)

    @Query("UPDATE products SET qty = 0, isSelected = 0")
    suspend fun clearAllQuantities()

    @Query("UPDATE products SET qty = :qty, isSelected = :isSelected WHERE code IN (:codes)")
    suspend fun updateBatchQuantity(codes: List<String>, qty: Int, isSelected: Boolean)
}
