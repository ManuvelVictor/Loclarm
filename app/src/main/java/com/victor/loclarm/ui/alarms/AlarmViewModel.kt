package com.victor.loclarm.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.victor.loclarm.db.Alarm
import com.victor.loclarm.db.AlarmDatabase
import com.victor.loclarm.db.AlarmRepository
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    val alarms: LiveData<List<Alarm>>

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        alarms = repository.alarms
    }

    fun insert(alarm: Alarm) = viewModelScope.launch {
        repository.insert(alarm)
    }
}

