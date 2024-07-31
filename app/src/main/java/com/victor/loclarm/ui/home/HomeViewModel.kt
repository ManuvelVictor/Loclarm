package com.victor.loclarm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.victor.loclarm.db.Alarm
import com.victor.loclarm.db.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AlarmRepository) : ViewModel() {
    fun saveAlarm(latLng: LatLng, alarmName: String, radius: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val alarm = Alarm(
                alarmName = alarmName,
                destinationLat = latLng.latitude,
                destinationLng = latLng.longitude,
                radius = radius,
                isActive = true
            )
            repository.insert(alarm)
        }
    }
}
