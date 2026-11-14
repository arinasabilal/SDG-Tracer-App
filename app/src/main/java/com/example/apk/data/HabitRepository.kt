// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/data/HabitRepository.kt
// Lapisan data: kebiasaan harian per pengguna di Cloud Firestore.
// Struktur: users/{uid}/habits/{habitId}  dan  users/{uid}/days/{yyyy-MM-dd} (field "done" = daftar id yang dicentang)
// Cache offline Firestore aktif otomatis, jadi pencatatan tetap jalan tanpa internet.
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HabitModule(val label: String) {
    HEALTH("Kesehatan"),
    SANITATION("Sanitasi"),
    EDUCATION("Pendidikan")
}

data class Habit(
    val id: String,
    val title: String,
    val module: HabitModule,
    val order: Long = 0L
)

object HabitRepository {

    // Kebiasaan bawaan untuk pengguna baru. Id tetap, jadi menulis ulang tidak membuat duplikat.
    private val defaults = listOf(
        Habit("sleep", "Tidur dan bangun tepat waktu", HabitModule.HEALTH),
        Habit("water", "Minum air cukup", HabitModule.HEALTH),
        Habit("meal", "Makan teratur", HabitModule.HEALTH),
        Habit("light_ac", "Matikan lampu dan AC/kipas", HabitModule.SANITATION),
        Habit("charger", "Cabut charger dan perangkat", HabitModule.SANITATION),
        Habit("focus", "Belajar fokus 25 menit", HabitModule.EDUCATION),
        Habit("tasks", "Cek tugas dan tenggat", HabitModule.EDUCATION)
    )

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    // Kunci tanggal hari ini menurut zona waktu HP, contoh: 2026-11-14.
    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Isi kebiasaan bawaan kalau pengguna belum punya satu pun.
    fun seedDefaultsIfEmpty() {
        val col = userDoc()?.collection("habits") ?: return
        col.limit(1).get().addOnSuccessListener { snap ->
            if (!snap.isEmpty) return@addOnSuccessListener
            defaults.forEachIndexed { index, h ->
                col.document(h.id).set(
                    mapOf(
                        "title" to h.title,
                        "module" to h.module.name,
                        "order" to index.toLong()
                    )
                )
            }
        }
    }

    // Pantau daftar kebiasaan. Simpan hasil return-nya dan panggil remove() saat layar ditutup.
    fun observeHabits(onChange: (List<Habit>) -> Unit): ListenerRegistration? {
        val col = userDoc()?.collection("habits") ?: return null
        return col.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val list = snap.documents.mapNotNull { d ->
                val title = d.getString("title") ?: return@mapNotNull null
                val module = runCatching { HabitModule.valueOf(d.getString("module") ?: "") }
                    .getOrNull() ?: return@mapNotNull null
                Habit(d.id, title, module, d.getLong("order") ?: 0L)
            }.sortedBy { it.order }
            onChange(list)
        }
    }

    // Pantau id kebiasaan yang sudah dicentang pada tanggal tertentu.
    fun observeDone(dateKey: String, onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val done = (snap.get("done") as? List<*>)
                ?.filterIsInstance<String>()?.toSet() ?: emptySet()
            onChange(done)
        }
    }

        // Pantau riwayat beberapa hari terakhir: tanggal -> id kebiasaan yang dicentang.
    fun observeHistory(limit: Long, onChange: (Map<String, Set<String>>) -> Unit): ListenerRegistration? {
        val col = userDoc()?.collection("days") ?: return null
        return col.orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val map = LinkedHashMap<String, Set<String>>()
                snap.documents.forEach { d ->
                    map[d.id] = (d.get("done") as? List<*>)
                        ?.filterIsInstance<String>()?.toSet() ?: emptySet()
                }
                onChange(map)
            }
    }
    
    // Centang atau batalkan satu kebiasaan pada tanggal tertentu.
    fun setDone(dateKey: String, habitId: String, done: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        val op = if (done) FieldValue.arrayUnion(habitId) else FieldValue.arrayRemove(habitId)
        ref.set(mapOf("done" to op), SetOptions.merge())
    }
}
