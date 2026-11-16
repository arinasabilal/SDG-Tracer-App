// LOKASI: app/src/main/java/com/example/apk/data/TaskRepository.kt (file BARU)
// Up Task: daftar tugas, pengaturan timer fokus, dan catatan sesi fokus harian.
// Struktur: users/{uid}/tasks/{id} (title, deadline, priority, done, createdAt)
//           users/{uid}/settings/focus (focusMinutes, breakMinutes)
//           users/{uid}/days/{tanggal} (focusMinutes, focusSessions)
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

// deadline = "yyyy-MM-dd", atau "" kalau tanpa tenggat. priority: 0 tinggi, 1 sedang, 2 rendah.
data class TaskItem(
    val id: String,
    val title: String,
    val deadline: String,
    val priority: Int,
    val done: Boolean,
    val createdAt: Long
)

data class FocusSettings(val focusMinutes: Int, val breakMinutes: Int) {
    companion object {
        val DEFAULT = FocusSettings(25, 5)
    }
}

object TaskRepository {

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    fun observeTasks(onChange: (List<TaskItem>) -> Unit): ListenerRegistration? {
        val col = userDoc()?.collection("tasks") ?: return null
        return col.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val list = snap.documents.mapNotNull { d ->
                val title = d.getString("title") ?: return@mapNotNull null
                TaskItem(
                    id = d.id,
                    title = title,
                    deadline = d.getString("deadline") ?: "",
                    priority = (d.getLong("priority") ?: 1L).toInt(),
                    done = d.getBoolean("done") ?: false,
                    createdAt = d.getLong("createdAt") ?: 0L
                )
            }
            onChange(list)
        }
    }

    fun addTask(title: String, deadline: String, priority: Int) {
        val col = userDoc()?.collection("tasks") ?: return
        col.add(
            mapOf(
                "title" to title.trim(),
                "deadline" to deadline,
                "priority" to priority,
                "done" to false,
                "createdAt" to System.currentTimeMillis()
            )
        )
    }

    fun setTaskDone(id: String, done: Boolean) {
        userDoc()?.collection("tasks")?.document(id)?.update("done", done)
    }

    fun deleteTask(id: String) {
        userDoc()?.collection("tasks")?.document(id)?.delete()
    }

    // Kalau belum pernah disimpan, dipakai nilai bawaan 25 menit fokus dan 5 menit istirahat.
    fun observeFocusSettings(onChange: (FocusSettings) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("settings")?.document("focus") ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val d = FocusSettings.DEFAULT
            onChange(
                FocusSettings(
                    focusMinutes = (snap.getLong("focusMinutes") ?: d.focusMinutes.toLong()).toInt(),
                    breakMinutes = (snap.getLong("breakMinutes") ?: d.breakMinutes.toLong()).toInt()
                )
            )
        }
    }

    fun saveFocusSettings(s: FocusSettings) {
        userDoc()?.collection("settings")?.document("focus")
            ?.set(
                mapOf("focusMinutes" to s.focusMinutes, "breakMinutes" to s.breakMinutes),
                SetOptions.merge()
            )
    }

    // onChange(totalMenitFokus, jumlahSesi) untuk tanggal tertentu.
    fun observeFocusToday(dateKey: String, onChange: (Int, Int) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            onChange(
                (snap.getLong("focusMinutes") ?: 0L).toInt(),
                (snap.getLong("focusSessions") ?: 0L).toInt()
            )
        }
    }

    fun addFocusSession(dateKey: String, minutes: Int) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        ref.set(
            mapOf(
                "focusMinutes" to FieldValue.increment(minutes.toLong()),
                "focusSessions" to FieldValue.increment(1L)
            ),
            SetOptions.merge()
        )
    }
}
