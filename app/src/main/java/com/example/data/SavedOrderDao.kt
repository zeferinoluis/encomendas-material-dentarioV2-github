package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedOrderDao {
    @Query("SELECT * FROM saved_orders ORDER BY createdAt DESC")
    fun getAllSavedOrders(): Flow<List<SavedOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SavedOrder)

    @Query("DELETE FROM saved_orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)
}
