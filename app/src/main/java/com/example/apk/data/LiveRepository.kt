// LOKASI: app/src/main/java/com/example/apk/data/LiveRepository.kt (file BARU)
// Up Live: pengaturan jam tidur + catatan makan harian.
// Struktur: users/{uid}/settings/live (bedMinutes, wakeMinutes, windDownMinutes)
//           users/{uid}/days/{tanggal} (field "meals" = daftar: breakfast, lunch, dinner)
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

// Semua waktu dalam menit sejak tengah malam (0 sampai 1439).
data class LiveSettings(
    val bedMinutes: Int,
    val wakeMinutes: Int,
    val windDownMinutes: Int
) {
    companion object {
        val DEFAULT = LiveSettings(bedMinutes = 22 * 60, wakeMinutes = 5 * 60 + 30, windDownMinutes = 30)
    }
}

object LiveRepository {

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    // Kalau belum pernah disimpan, dipakai nilai bawaan.
    fun observeSettings(onChange: (LiveSettings) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("settings")?.document("live") ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val d = LiveSettings.DEFAULT
            onChange(
                LiveSettings(
                    bedMinutes = (snap.getLong("bedMinutes") ?: d.bedMinutes.toLong()).toInt(),
                    wakeMinutes = (snap.getLong("wakeMinutes") ?: d.wakeMinutes.toLong()).toInt(),
                    windDownMinutes = (snap.getLong("windDownMinutes") ?: d.windDownMinutes.toLong()).toInt()
                )
            )
        }
    }

    fun saveSettings(s: LiveSettings) {
        userDoc()?.collection("settings")?.document("live")
            ?.set(
                mapOf(
                    "bedMinutes" to s.bedMinutes,
                    "wakeMinutes" to s.wakeMinutes,
                    "windDownMinutes" to s.windDownMinutes
                ),
                SetOptions.merge()
            )
    }

    fun observeMeals(dateKey: String, onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val meals = (snap.get("meals") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            onChange(meals)
        }
    }

    fun setMeal(dateKey: String, meal: String, on: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        val op = if (on) FieldValue.arrayUnion(meal) else FieldValue.arrayRemove(meal)
        ref.set(mapOf("meals" to op), SetOptions.merge())
    }
}
