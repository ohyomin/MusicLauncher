package com.carlinkit.musicstarter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlinkit.musicstarter.MusicLauncher

class MyService : Service() {

    companion object {
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "primary_notification_channel"
    }

    lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Launcher")
            .setContentText("Music Launcher")
            .build()
        Log.d("hmhm", "start foreground")

        startForeground(NOTIFICATION_ID, notification)

        MusicLauncher(this).play(callback = { handler.postDelayed({ stopSelf() }, 5000)})
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "MusicLauncher",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.description = "Music laucnher"
        notificationChannel.enableLights(false)
        notificationChannel.enableVibration(false)

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}