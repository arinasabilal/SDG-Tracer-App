package com.example.apk.notif

import android.content.Context

object ReminderPrefs {
    private const val FILE = "reminder_prefs"
    private const val SEP = '\u0001'

    private fun sp(c: Context) = c.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // Saklar utama "Smart Reminder" di Profil.
    fun enabled(c: Context): Boolean = sp(c).getBoolean("enabled", true)
    fun setEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("enabled", v).apply()

    // Semua waktu: menit sejak tengah malam (0 sampai 1439).
    fun bed(c: Context): Int = sp(c).getInt("bed", 22 * 60)
    fun wake(c: Context): Int = sp(c).getInt("wake", 6 * 60)
    fun windDown(c: Context): Int = sp(c).getInt("windDown", 30)
    fun depart(c: Context): Int = sp(c).getInt("depart", 7 * 60)

    fun setDepart(c: Context, minutes: Int) = sp(c).edit().putInt("depart", minutes).apply()

    fun setSleep(c: Context, bed: Int, wake: Int, windDown: Int) {
        sp(c).edit().putInt("bed", bed).putInt("wake", wake).putInt("windDown", windDown).apply()
    }

    // Tugas yang belum selesai dan punya tenggat: pasangan (tenggat "yyyy-MM-dd", judul).
    fun tasks(c: Context): List<Pair<String, String>> {
        val set = sp(c).getStringSet("tasks", emptySet()) ?: emptySet()
        return set.mapNotNull {
            val i = it.indexOf(SEP)
            if (i <= 0) null else it.substring(0, i) to it.substring(i + 1)
        }.sortedBy { it.first }
    }

    fun setTasks(c: Context, list: List<Pair<String, String>>) {
        val set = list.map { "${it.first}$SEP${it.second}" }.toSet()
        sp(c).edit().putStringSet("tasks", set).apply()
    }

    // Dipanggil saat keluar akun: data pengguna lama tidak boleh memicu pengingat lagi.
    fun clearUserData(c: Context) {
        sp(c).edit().remove("tasks").remove("bed").remove("wake").remove("windDown").apply()
    }
}
