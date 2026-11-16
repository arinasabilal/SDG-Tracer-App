// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/data/WaterRepository.kt
// Data fitur Minum Air Putih (Up Live).
// Struktur: users/{uid}/days/{tanggal}  -> field "water" (jumlah gelas hari itu)
//           users/{uid}/settings/water  -> targetGlasses, periodDays (0 = tak terbatas), startDate
// Memakai dokumen "days" yang sama dengan Up Save, jadi tidak menambah koleksi baru.
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

// periodDays: 7 = 1 minggu, 30 = 1 bulan, 0 = tak terbatas.
data class WaterPlan(val targetGlasses: Int, val periodDays: Int, val startDate: String)

object WaterRepository {

    const val DEFAULT_TARGET = 8

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    // Jumlah gelas yang sudah diminum pada tanggal tertentu.
    fun observeWater(dateKey: String, onChange: (Int) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            onChange((snap.getLong("water") ?: 0L).toInt())
        }
    }

    fun setWater(dateKey: String, count: Int) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        ref.set(mapOf("water" to count.toLong()), SetOptions.merge())
    }

    // onChange(null) = target belum pernah disimpan (pakai nilai bawaan).
    fun observePlan(onChange: (WaterPlan?) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("settings")?.document("water") ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            if (!snap.exists()) {
                onChange(null)
                return@addSnapshotListener
            }
            onChange(
                WaterPlan(
                    (snap.getLong("targetGlasses") ?: DEFAULT_TARGET.toLong()).toInt(),
                    (snap.getLong("periodDays") ?: 0L).toInt(),
                    snap.getString("startDate") ?: HabitRepository.todayKey()
                )
            )
        }
    }

    fun savePlan(targetGlasses: Int, periodDays: Int, startDate: String) {
        userDoc()?.collection("settings")?.document("water")
            ?.set(
                mapOf(
                    "targetGlasses" to targetGlasses,
                    "periodDays" to periodDays,
                    "startDate" to startDate
                )
            )
    }
}
