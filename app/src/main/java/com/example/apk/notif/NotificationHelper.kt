package com.example.apk.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.apk.MainActivity
import com.example.apk.R

object NotificationHelper {
    const val CH_REMINDER = "reminder"
    const val CH_TIMER = "timer"

    fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(CH_REMINDER, "Pengingat harian", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Jam tidur, jeda layar, jam berangkat, makan, minum, dan tenggat tugas"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_TIMER, "Timer fokus", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Bunyi saat sesi fokus atau istirahat selesai"
            }
        )
    }

    fun hasPermission(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun show(ctx: Context, id: Int, title: String, text: String, channel: String) {
        if (!hasPermission(ctx)) return
        ensureChannels(ctx)
        val open = PendingIntent.getActivity(
            ctx,
            0,
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(id, n)
        } catch (_: SecurityException) {
            // Izin dicabut saat notifikasi dikirim: abaikan.
        }
    }
}
