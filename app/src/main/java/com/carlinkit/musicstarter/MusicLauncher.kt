package com.carlinkit.musicstarter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MusicLauncher(private val context: Context) {
    companion object {
        private var mediaBrowser: MediaBrowser? = null
        const val TAG = "MusicLauncher"
    }



    private fun getComponent(): ComponentName? {
        return context.getSharedPreferences("play_music", AppCompatActivity.MODE_PRIVATE).run {
            val packageName = getString("package", null) ?: return@run null
            val name= getString("name", null) ?: return@run null
            ComponentName(packageName, name)
        }
    }

    fun play(componentName: ComponentName? = getComponent()) {
        if (componentName == null) return

        mediaBrowser?.disconnect()
        mediaBrowser = MediaBrowser(
            context,
            componentName,
            object : MediaBrowser.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()
                    Log.i(TAG, "onConnected ${componentName.packageName}")
                    simulateMediaButton(componentName.packageName)
                }

                override fun onConnectionFailed() {
                    super.onConnectionFailed()
                    Log.e(TAG, "onConnectionFailed ${componentName.packageName}")
                    simulateMediaButton(componentName.packageName)

                    thread {
                        Thread.sleep(3000)
                        sendMediaKeyEvent()
                    }
                }
            },
            Bundle()
        ).also { it.connect() }
    }

    fun simulateMediaButton(packageName: String) {
        val eventTime = SystemClock.uptimeMillis() - 1

        val keyEventDown =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
        val keyEventUp =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0)

        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            setPackage(packageName)
        }

        context.sendBroadcast(intent.apply { putExtra(Intent.EXTRA_KEY_EVENT, keyEventDown) })
        context.sendBroadcast(intent.apply { putExtra(Intent.EXTRA_KEY_EVENT, keyEventUp) })
    }

    fun sendMediaKeyEvent() {
        Log.e("hmhm", "hmhm sendmediakey")
        val eventTime = SystemClock.uptimeMillis() - 1

        val keyEventDown =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
        val keyEventUp =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(keyEventDown)
        audioManager.dispatchMediaKeyEvent(keyEventUp)
    }
}