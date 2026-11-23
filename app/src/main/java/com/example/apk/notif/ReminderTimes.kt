package com.example.apk.notif

import java.util.Calendar

object ReminderTimes {

    // Waktu (epoch ms) berikutnya untuk jam [minutes] (menit sejak tengah malam), SESUDAH [nowMs].
    fun nextDaily(nowMs: Long, minutes: Int): Long {
        val m = ((minutes % 1440) + 1440) % 1440
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, m / 60)
            set(Calendar.MINUTE, m % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMs) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    // Waktu (epoch ms) pada tanggal [dateKey] ("yyyy-MM-dd") jam [minutes], digeser [dayOffset] hari.
    // Mengembalikan null kalau format tanggal salah.
    fun onDate(dateKey: String, minutes: Int, dayOffset: Int = 0): Long? {
        val p = dateKey.split("-")
        if (p.size != 3) return null
        val y = p[0].toIntOrNull() ?: return null
        val mo = p[1].toIntOrNull() ?: return null
        val d = p[2].toIntOrNull() ?: return null
        val cal = Calendar.getInstance().apply {
            clear()
            set(y, mo - 1, d, minutes / 60, minutes % 60, 0)
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        return cal.timeInMillis
    }

    fun fmt(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        return "%02d.%02d".format(m / 60, m % 60)
    }
}
