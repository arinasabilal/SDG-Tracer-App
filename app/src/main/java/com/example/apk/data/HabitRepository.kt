// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/data/HabitRepository.kt
// Lapisan data: kebiasaan harian per pengguna di Cloud Firestore.
// Struktur: users/{uid}/habits/{habitId}  dan  users/{uid}/days/{yyyy-MM-dd} (field "done" = daftar id yang dicentang)
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    // Rutinitas bawaan. pre_leave dan pre_sleep dicentang otomatis oleh layar Up Save
    // saat semua perangkat sudah dimatikan.
    private val defaults = listOf(
        Habit("sleep", "Tidur dan bangun tepat waktu", HabitModule.HEALTH),
        Habit("water", "Minum air cukup", HabitModule.HEALTH),
        Habit("meal", "Makan teratur", HabitModule.HEALTH),
        Habit("pre_leave", "Cek perangkat sebelum berangkat", HabitModule.SANITATION),
        Habit("pre_sleep", "Cek perangkat sebelum tidur", HabitModule.SANITATION),
        Habit("focus", "Belajar fokus 25 menit", HabitModule.EDUCATION),
        Habit("tasks", "Cek tugas dan tenggat", HabitModule.EDUCATION)
    )

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun habitMap(h: Habit, index: Int) = mapOf(
        "title" to h.title,
        "module" to h.module.name,
        "order" to index.toLong()
    )

    // Pengguna baru: isi semua rutinitas bawaan. Pengguna lama: pindahkan Sanitasi ke versi baru.
    fun seedDefaultsIfEmpty() {
        val col = userDoc()?.collection("habits") ?: return
        col.limit(1).get().addOnSuccessListener { snap ->
            if (snap.isEmpty) {
                defaults.forEachIndexed { index, h -> col.document(h.id).set(habitMap(h, index)) }
            } else {
                col.document("pre_leave").get().addOnSuccessListener { d ->
                    if (!d.exists()) migrateSanitation(col)
                }
            }
        }
    }

    private fun migrateSanitation(col: CollectionReference) {
        defaults.forEachIndexed { index, h ->
            if (h.id == "pre_leave" || h.id == "pre_sleep") {
                col.document(h.id).set(habitMap(h, index))
            }
        }
        col.document("light_ac").delete()
        col.document("charger").delete()
    }

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

    fun observeDone(dateKey: String, onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val done = (snap.get("done") as? List<*>)
                ?.filterIsInstance<String>()?.toSet() ?: emptySet()
            onChange(done)
        }
    }

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

    fun setDone(dateKey: String, habitId: String, done: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        val op = if (done) FieldValue.arrayUnion(habitId) else FieldValue.arrayRemove(habitId)
        ref.set(mapOf("done" to op), SetOptions.merge())
    }
}
