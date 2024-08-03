package com.victor.loclarm.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victor.loclarm.db.model.Alarm

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, isActive: Boolean)

    @Query("SELECT * FROM alarms WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAlarm(): Alarm?

    @Query("SELECT * FROM alarms WHERE alarmName = :name LIMIT 1")
    suspend fun getAlarmByName(name: String): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAlarms(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE isActive = 1")
    fun getActiveAlarms(): LiveData<List<Alarm>>

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)
}
