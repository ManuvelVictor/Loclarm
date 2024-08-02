package com.victor.loclarm.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.victor.loclarm.db.AlarmDatabase
import com.victor.loclarm.db.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AlarmDatabase.getDatabase(application)

    private val _userSettings = MutableLiveData<UserSettings?>()
    val userSettings: LiveData<UserSettings?> get() = _userSettings

    fun loadSettings() {
        viewModelScope.launch {
            val settings = withContext(Dispatchers.IO) {
                db.userSettingsDao().getUserSettings()
            }
            _userSettings.value = settings
        }
    }

    fun saveSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.userSettingsDao().upsert(userSettings)
            }
        }
    }
}