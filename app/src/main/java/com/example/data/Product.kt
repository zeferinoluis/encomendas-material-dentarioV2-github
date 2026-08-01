package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val code: String,
    val company: String,
    val description: String,
    val priceRange: String,
    val minPrice: Double,
    val maxPrice: Double,
    val invoicesCount: Int,
    val qty: Int = 0,
    val isSelected: Boolean = false,
    val isFavorite: Boolean = false
)

