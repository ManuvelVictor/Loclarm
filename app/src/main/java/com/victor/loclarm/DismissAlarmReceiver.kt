package com.victor.loclarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.victor.loclarm.db.AppDatabase
import com.victor.loclarm.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = AppDatabase.getDatabase(context)
        val alarmDao = db.alarmDao()
        CoroutineScope(Dispatchers.IO).launch {
            val activeAlarm = alarmDao.getActiveAlarm()
            activeAlarm?.let {
                alarmDao.updateAlarmStatus(it.id, false)
            }
        }
        val serviceIntent = Intent(context, LocationService::class.java)
        context.stopService(serviceIntent)
    }
}