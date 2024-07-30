package com.victor.loclarm.db

import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val alarms: LiveData<List<Alarm>> = alarmDao.getAlarms()

    suspend fun insert(alarm: Alarm) {
        alarmDao.insert(alarm)
    }
}
