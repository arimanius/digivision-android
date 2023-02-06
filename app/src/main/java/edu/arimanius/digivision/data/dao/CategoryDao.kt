package edu.arimanius.digivision.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.arimanius.digivision.data.entity.CategoryHistory

@Dao
interface CategoryDao: InsertableDao<CategoryHistory> {
    @Query("SELECT * FROM categories WHERE url = :url")
    suspend fun getByUrl(url: String): CategoryHistory?

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryHistory?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insert(entity: CategoryHistory): Long
}