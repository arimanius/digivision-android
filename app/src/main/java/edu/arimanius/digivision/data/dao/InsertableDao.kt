package edu.arimanius.digivision.data.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy


interface InsertableDao<T> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: T): Long
}
