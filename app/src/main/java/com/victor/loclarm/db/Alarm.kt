package com.victor.loclarm.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alarmName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val radius: Int,
    val isActive: Boolean = false
)

