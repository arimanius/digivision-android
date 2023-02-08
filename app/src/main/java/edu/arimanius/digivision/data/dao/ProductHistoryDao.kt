package edu.arimanius.digivision.data.dao

import androidx.room.Dao
import androidx.room.Query
import edu.arimanius.digivision.data.entity.ProductHistory

@Dao
interface ProductHistoryDao: InsertableDao<ProductHistory> {
    @Query("SELECT * FROM products WHERE historyId = :historyId ORDER BY score DESC")
    suspend fun getByHistoryId(historyId: Long): List<ProductHistory>

    @Query("DELETE FROM products WHERE historyId = :historyId")
    suspend fun deleteByHistoryId(historyId: Long)
}