package com.ohmnia.musicstarter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.ohmnia.musicstarter.service.MyService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, MyService::class.java))
    }
}