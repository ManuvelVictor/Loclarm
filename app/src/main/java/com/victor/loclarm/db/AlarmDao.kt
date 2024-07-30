package com.victor.loclarm.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, isActive: Boolean)

    @Query("SELECT * FROM alarms WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAlarm(): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAlarms(): LiveData<List<Alarm>>
}
