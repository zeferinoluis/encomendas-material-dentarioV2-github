package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_orders")
data class SavedOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val clinicName: String = "Clínica de Aver-o-Mar / Meadela",
    val itemCount: Int,
    val totalUnits: Int,
    val totalPrice: Double,
    val itemsSummary: String,
    val notes: String = ""
)
