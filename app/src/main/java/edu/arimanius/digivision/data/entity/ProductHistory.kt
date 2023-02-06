package edu.arimanius.digivision.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = History::class,
            parentColumns = ["id"],
            childColumns = ["historyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["historyId"])
    ]
)
data class ProductHistory(
    var productId: Int,
    var title: String,
    var url: String,
    var status: String,
    var price: Long,
    var imageUrl: String,
    var rate: Int,
    var rateCount: Int,
    var categoryIds: String,
    var score: Float,
    var historyId: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}