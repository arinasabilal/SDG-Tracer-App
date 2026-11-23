package com.example.apk.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderScheduler {
    const val EXTRA_TITLE = "title"
    const val EXTRA_TEXT = "text"
    const val EXTRA_CODE = "code"
    const val EXTRA_TIMER = "timer"
    private const val ACTION = "com.example.apk.REMINDER"

    // Kode alarm (harus unik per jenis pengingat).
    private const val ID_WIND = 101
    private const val ID_BED = 102
    private const val ID_WAKE = 103
    private const val ID_DEPART = 104
    private const val ID_MEAL = 111       // 111 sarapan, 112 makan siang, 113 makan malam
    private const val ID_WATER = 121      // 121 sampai 126
    private const val ID_TIMER = 900
    private const val ID_TASK = 2000      // 2000 sampai 2000 + MAX_TASK_ALARMS - 1
    private const val MAX_TASK_ALARMS = 40

    private val MEALS = listOf(
        Triple(111, 7 * 60 + 30, "Waktunya sarapan" to "Isi tenaga dulu sebelum kelas. Centang di Up Live."),
        Triple(112, 12 * 60 + 30, "Waktunya makan siang" to "Jangan lewatkan makan di sela jadwal. Centang di Up Live."),
        Triple(113, 18 * 60 + 30, "Waktunya makan malam" to "Makan teratur menjaga ritme tubuh. Centang di Up Live.")
    )
    private val WATER_HOURS = listOf(9, 11, 13, 15, 17, 19)

    // Diisi MainActivity supaya FocusTimer (object tanpa Context) bisa menjadwalkan alarm.
    @Volatile
    var appContext: Context? = null

    fun canExact(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return false
        return am.canScheduleExactAlarms()
    }

    // ------------------------------------------------------------ PENJADWALAN UTAMA

    // Hitung ulang semua alarm dari data lokal. Aman dipanggil berkali-kali.
    fun rescheduleAll(c: Context) {
        val ctx = c.applicationContext
        cancelAllExceptTimer(ctx)
        if (!ReminderPrefs.enabled(ctx)) return

        val now = System.currentTimeMillis()
        val bed = ReminderPrefs.bed(ctx)
        val wake = ReminderPrefs.wake(ctx)
        val wind = ReminderPrefs.windDown(ctx)
        val depart = ReminderPrefs.depart(ctx)

        if (wind > 0) {
            daily(
                ctx, ID_WIND, now, bed - wind,
                "Jeda layar dulu",
                "Sebentar lagi jam tidur (${ReminderTimes.fmt(bed)}). Letakkan HP dan bersiap tidur."
            )
        }
        daily(
            ctx, ID_BED, now, bed,
            "Waktunya tidur",
            "Matikan lampu, AC/kipas, dan cabut charger sebelum tidur. Centang di Up Save."
        )
        daily(
            ctx, ID_WAKE, now, wake,
            "Selamat pagi",
            "Waktunya bangun. Tandai bangun tepat waktu di Up Live."
        )
        daily(
            ctx, ID_DEPART, now, depart,
            "Mau berangkat?",
            "Cek dulu: lampu, AC/kipas, charger, dan perangkat lain sudah mati? Centang di Up Save."
        )
        for ((code, minutes, msg) in MEALS) {
            daily(ctx, code, now, minutes, msg.first, msg.second)
        }
        WATER_HOURS.forEachIndexed { i, hour ->
            daily(
                ctx, ID_WATER + i, now, hour * 60,
                "Minum air dulu",
                "Catat satu gelas di Up Live supaya target hari ini tercapai."
            )
        }
        scheduleTaskDeadlines(ctx, now)
    }

    private fun daily(
        ctx: Context,
        code: Int,
        now: Long,
        minutes: Int,
        title: String,
        text: String
    ) {
        val at = ReminderTimes.nextDaily(now, minutes)
        setAlarm(ctx, at, pending(ctx, code, title, text, timer = false, create = true) ?: return)
    }

    // Dua pengingat per tugas: sore hari sebelum tenggat (18.00) dan pagi hari tenggat (07.30).
    private fun scheduleTaskDeadlines(ctx: Context, now: Long) {
        val events = ArrayList<Triple<Long, String, String>>()
        for ((deadline, title) in ReminderPrefs.tasks(ctx)) {
            val eve = ReminderTimes.onDate(deadline, 18 * 60, -1)
            val day = ReminderTimes.onDate(deadline, 7 * 60 + 30, 0)
            if (eve != null && eve > now) events.add(Triple(eve, "Tenggat besok", title))
            if (day != null && day > now) events.add(Triple(day, "Tenggat hari ini", title))
        }
        events.sortBy { it.first }
        events.take(MAX_TASK_ALARMS).forEachIndexed { i, e ->
            val pi = pending(ctx, ID_TASK + i, e.second, e.third, timer = false, create = true) ?: return@forEachIndexed
            setAlarm(ctx, e.first, pi)
        }
    }

    // ------------------------------------------------------------ TIMER FOKUS

    // Alarm satu kali saat sesi fokus/istirahat selesai, supaya tetap bunyi walau aplikasi ditutup.
    fun scheduleTimer(ctx: Context, seconds: Int, title: String, text: String) {
        val app = ctx.applicationContext
        val at = System.currentTimeMillis() + seconds * 1000L
        setAlarm(app, at, pending(app, ID_TIMER, title, text, timer = true, create = true) ?: return)
    }

    fun cancelTimer(ctx: Context) = cancel(ctx.applicationContext, ID_TIMER)

    // ------------------------------------------------------------ BATAL

    fun cancelAll(ctx: Context) {
        val app = ctx.applicationContext
        cancelAllExceptTimer(app)
        cancel(app, ID_TIMER)
    }

    private fun cancelAllExceptTimer(ctx: Context) {
        val codes = ArrayList<Int>()
        codes.addAll(listOf(ID_WIND, ID_BED, ID_WAKE, ID_DEPART))
        codes.addAll(MEALS.map { it.first })
        codes.addAll(WATER_HOURS.indices.map { ID_WATER + it })
        codes.addAll((0 until MAX_TASK_ALARMS).map { ID_TASK + it })
        for (code in codes) cancel(ctx, code)
    }

    private fun cancel(ctx: Context, code: Int) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(ctx, ReminderReceiver::class.java).apply { action = ACTION }
        val pi = PendingIntent.getBroadcast(
            ctx, code, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        ) ?: return
        am.cancel(pi)
        pi.cancel()
    }

    // ------------------------------------------------------------ BANTUAN

    private fun pending(
        ctx: Context,
        code: Int,
        title: String,
        text: String,
        timer: Boolean,
        create: Boolean
    ): PendingIntent? {
        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            action = ACTION
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_CODE, code)
            putExtra(EXTRA_TIMER, timer)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE)
        return PendingIntent.getBroadcast(ctx, code, intent, flags)
    }

    // Alarm tepat waktu kalau diizinkan; kalau tidak, alarm biasa (bisa meleset beberapa menit).
    private fun setAlarm(ctx: Context, at: Long, pi: PendingIntent) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        try {
            if (canExact(ctx)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
