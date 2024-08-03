package com.victor.loclarm.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.victor.loclarm.MainActivity
import com.victor.loclarm.R
import com.victor.loclarm.db.AlarmDao
import com.victor.loclarm.db.AlarmDatabase
import com.victor.loclarm.db.UserSettingsDao
import com.victor.loclarm.db.model.Alarm
import com.victor.loclarm.db.model.UserSettings
import com.victor.loclarm.receiver.DismissAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var alarmName: String = ""
    private var enteredRadius = false
    private var destinationLat: Double = 0.0
    private var destinationLng: Double = 0.0
    private var radius: Int = 100
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var userSettings: UserSettings
    private lateinit var audioManager: AudioManager

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
    }

    private lateinit var alarmDao: AlarmDao
    private lateinit var userSettingsDao: UserSettingsDao

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ServiceCast")
    override fun onCreate() {
        super.onCreate()
        val db = AlarmDatabase.getDatabase(this)
        alarmDao = db.alarmDao()
        userSettingsDao = db.userSettingsDao()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        CoroutineScope(Dispatchers.IO).launch {
            userSettings = fetchUserSettings()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        vibrator = getSystemService(Vibrator::class.java)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    handleLocationUpdate(location)
                }
            }
        }
        startLocationUpdates()
    }

    private suspend fun fetchUserSettings(): UserSettings {
        return withContext(Dispatchers.IO) {
            userSettingsDao.getUserSettings() ?: UserSettings()
        }
    }

    private fun triggerAlarm() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.retro_game)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            val volumeSetting = userSettings.volume
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumeIndex = ((volumeSetting / 10.0) * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex, 0)
            if (userSettings.vibration) {
                startVibration()
            }
            showEnterRadiusNotification()
        }
    }

    private fun startVibration() {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun showEnterRadiusNotification() {
        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enter Radius Alert")
            .setContentText("You have entered the specified radius")
            .setSmallIcon(R.drawable.target_icon)
            .addAction(R.drawable.close_icon, "Dismiss", dismissPendingIntent)
            .setOngoing(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000
        ).apply {
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(5000)
            setMaxUpdateDelayMillis(10000)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun handleLocationUpdate(location: Location) {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            destinationLat,
            destinationLng,
            results
        )
        val distanceInMeters = results[0]
        if (distanceInMeters <= radius && !enteredRadius) {
            enteredRadius = true
            triggerAlarm()
            showEnterRadiusNotification()
        } else if (distanceInMeters > radius && enteredRadius) {
            enteredRadius = false
            stopAlarm()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
                it.release()
                mediaPlayer = null
            }
        }
        stopVibration()

        CoroutineScope(Dispatchers.IO).launch {
            val activeAlarm = alarmDao.getActiveAlarm()
            activeAlarm?.let {
                alarmDao.updateAlarmStatus(it.id, false)
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        destinationLat = intent?.getDoubleExtra("destinationLat", 0.0) ?: 0.0
        destinationLng = intent?.getDoubleExtra("destinationLng", 0.0) ?: 0.0
        radius = intent?.getIntExtra("radius", 100) ?: 100
        alarmName = intent?.getStringExtra("alarmName").toString()

        CoroutineScope(Dispatchers.IO).launch {
            val activeAlarm = alarmDao.getActiveAlarm()
            activeAlarm?.let {
                alarmDao.updateAlarmStatus(it.id, false)
            }

            val newAlarm = Alarm(
                alarmName = alarmName,
                destinationLat = destinationLat,
                destinationLng = destinationLng,
                radius = radius,
                isActive = true
            )
            alarmDao.insert(newAlarm)
        }

        val notification = createNotification()
        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.map_pin)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}