package com.victor.loclarm.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alarmName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val radius: Int,
    var isActive: Boolean = false
)