package edu.arimanius.digivision.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["title"], unique = true),
        Index(value = ["url"], unique = true),
    ]
)
data class CategoryHistory(
    var title: String,
    var url: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
