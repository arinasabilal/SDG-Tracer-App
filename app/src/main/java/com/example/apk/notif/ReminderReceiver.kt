package com.example.apk.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Dipanggil AlarmManager saat waktu pengingat tiba.
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val isTimer = intent.getBooleanExtra(ReminderScheduler.EXTRA_TIMER, false)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT) ?: ""
        val code = intent.getIntExtra(ReminderScheduler.EXTRA_CODE, 0)

        // Timer fokus bunyi walau Smart Reminder dimatikan (pengguna sendiri yang menjalankannya).
        if (isTimer || ReminderPrefs.enabled(app)) {
            NotificationHelper.show(
                app,
                code,
                title,
                text,
                if (isTimer) NotificationHelper.CH_TIMER else NotificationHelper.CH_REMINDER
            )
        }
        // Jadwalkan ulang pengingat harian untuk besok (dan tenggat yang masih akan datang).
        if (!isTimer) ReminderScheduler.rescheduleAll(app)
    }
}
