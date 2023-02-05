package edu.arimanius.digivision.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.arimanius.digivision.data.converter.TimestampConverter
import edu.arimanius.digivision.data.dao.CategoryDao
import edu.arimanius.digivision.data.dao.HistoryDao
import edu.arimanius.digivision.data.dao.ProductHistoryDao
import edu.arimanius.digivision.data.entity.CategoryHistory
import edu.arimanius.digivision.data.entity.History
import edu.arimanius.digivision.data.entity.ProductHistory

@Database(
    entities = [
        CategoryHistory::class,
        ProductHistory::class,
        History::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    TimestampConverter::class,
)
abstract class DigivisionDatabase: RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productHistoryDao(): ProductHistoryDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: DigivisionDatabase? = null

        fun getInstance(context: Context): DigivisionDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DigivisionDatabase::class.java,
                    "digivision_database"
                ).build()
            }
        }
    }
}