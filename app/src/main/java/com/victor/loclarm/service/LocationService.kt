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
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.victor.loclarm.MainActivity
import com.victor.loclarm.R

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var enteredRadius = false
    private var destinationLat: Double = 0.0
    private var destinationLng: Double = 0.0
    private var radius: Int = 100
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "Service created")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    handleLocationUpdate(location)
                }
            }
        }
        startLocationUpdates()
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

    private fun triggerAlarm() {
        Log.d("LocationService", "Triggering alarm")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.retro_game)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    private fun stopAlarm() {
        Log.d("LocationService", "Stopping alarm")
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
                it.release()
                mediaPlayer = null
            }
        }
    }

    private fun showEnterRadiusNotification() {
        Log.d("LocationService", "Showing notification")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enter Radius Alert")
            .setContentText("You have entered the specified radius")
            .setSmallIcon(R.drawable.target_icon)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        destinationLat = intent?.getDoubleExtra("destinationLat", 0.0) ?: 0.0
        destinationLng = intent?.getDoubleExtra("destinationLng", 0.0) ?: 0.0
        radius = intent?.getIntExtra("radius", 100) ?: 100

        val notification = createNotification()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.map_pin)
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
        Log.d("LocationService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}