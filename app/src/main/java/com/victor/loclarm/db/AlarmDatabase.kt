package com.victor.loclarm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile private var instance: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AlarmDatabase::class.java, "alarm_database")
                    .build()
                    .also { instance = it }
            }

    }
}
