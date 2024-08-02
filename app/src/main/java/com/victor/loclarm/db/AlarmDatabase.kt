package com.victor.loclarm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.victor.loclarm.db.model.Alarm
import com.victor.loclarm.db.model.UserSettings

@Database(entities = [Alarm::class, UserSettings::class], version = 2)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var instance: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    AlarmDatabase::class.java,
                    "alarm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }

    }
}
