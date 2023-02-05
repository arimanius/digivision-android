package edu.arimanius.digivision.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import edu.arimanius.digivision.data.entity.History

@Dao
interface HistoryDao: InsertableDao<History> {
    @Query("SELECT * FROM histories")
    fun getAll(): LiveData<List<History>>

    @Query("SELECT * FROM histories WHERE id = :id")
    suspend fun getById(id: Long): History?
}