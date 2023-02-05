package edu.arimanius.digivision.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "histories",
)
data class History(
    var imageUri: String,
    var createdAt: Date = Date()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
