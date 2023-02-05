package edu.arimanius.digivision.data.converter

import androidx.room.TypeConverter
import java.util.Date

class TimestampConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}
