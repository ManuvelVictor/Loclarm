package com.victor.loclarm.db

import androidx.lifecycle.LiveData
import com.victor.loclarm.db.model.Alarm

class AlarmRepository(private val alarmDao: AlarmDao) {

    val alarms: LiveData<List<Alarm>> = alarmDao.getAlarms()

    suspend fun insert(alarm: Alarm) {
        alarmDao.insert(alarm)
    }

    suspend fun getActiveAlarm(): Alarm? {
        return alarmDao.getActiveAlarm()
    }

    suspend fun getAlarmByName(name: String): Alarm? {
        return alarmDao.getAlarmByName(name)
    }

    suspend fun updateAlarmStatus(id: Int, isActive: Boolean) {
        alarmDao.updateAlarmStatus(id, isActive)
    }

    fun getActiveAlarms(): LiveData<List<Alarm>> {
        return alarmDao.getActiveAlarms()
    }

    suspend fun update(alarm: Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: Alarm) {
        alarmDao.delete(alarm)
    }

}
