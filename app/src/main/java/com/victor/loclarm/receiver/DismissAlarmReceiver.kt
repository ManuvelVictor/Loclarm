package com.victor.loclarm.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.victor.loclarm.db.AlarmDatabase
import com.victor.loclarm.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = AlarmDatabase.getDatabase(context)
        val alarmDao = db.alarmDao()
        CoroutineScope(Dispatchers.IO).launch {
            val activeAlarm = alarmDao.getActiveAlarm()
            activeAlarm?.let {
                alarmDao.updateAlarmStatus(it.id, false)
            }
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        context.stopService(Intent(context, LocationService::class.java))
    }
}
