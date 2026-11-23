package com.example.apk.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Alarm hilang saat HP restart, aplikasi diperbarui, atau jam/zona waktu diubah: jadwalkan ulang dari data lokal.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context.applicationContext)
    }
}
