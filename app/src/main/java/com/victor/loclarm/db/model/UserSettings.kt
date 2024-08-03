package com.victor.loclarm.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 0,
    val language: String = "English",
    val units: String = "Metric",
    val ringtone: String = "default",
    val volume: Int = 0,
    val vibration: Boolean = true
)
