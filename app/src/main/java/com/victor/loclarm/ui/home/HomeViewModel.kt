package com.victor.loclarm.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.victor.loclarm.db.model.Alarm
import com.victor.loclarm.db.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AlarmRepository) : ViewModel() {
    fun saveAlarm(latLng: LatLng, alarmName: String, radius: Int, onDuplicateName: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingAlarm = repository.getAlarmByName(alarmName)
            if (existingAlarm != null) {
                viewModelScope.launch(Dispatchers.Main) {
                    onDuplicateName()
                }
            } else {
                val alarm = Alarm(
                    alarmName = alarmName,
                    destinationLat = latLng.latitude,
                    destinationLng = latLng.longitude,
                    radius = radius,
                    isActive = true
                )
                Log.d("HomeViewModel", "Saving alarm: $alarm")
                repository.insert(alarm)
            }
        }
    }

    fun getActiveAlarms(): LiveData<List<Alarm>> {
        return repository.getActiveAlarms()
    }
}
