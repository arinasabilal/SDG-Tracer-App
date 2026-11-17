// LOKASI: app/src/main/java/com/example/apk/data/LiveStore.kt (file BARU)
// Up Live: jadwal tidur, status tidur/bangun, dan catatan makan.
// Memakai dokumen yang sama dengan sebelumnya, jadi jam tidur yang sudah tersimpan tetap terbaca.
//   users/{uid}/settings/live      -> bedMinutes, wakeMinutes, windDownMinutes
//   users/{uid}/days/{tanggal}     -> sleepBed, sleepWake (boolean), meals (daftar)
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

// Waktu dalam menit sejak tengah malam (0 sampai 1439).
data class SleepPlan(
    val bedMinutes: Int,
    val wakeMinutes: Int,
    val windDownMinutes: Int
) {
    companion object {
        val DEFAULT = SleepPlan(bedMinutes = 22 * 60, wakeMinutes = 6 * 60, windDownMinutes = 30)
    }
}

object LiveStore {

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    fun observeSleep(onChange: (SleepPlan) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("settings")?.document("live") ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val d = SleepPlan.DEFAULT
            onChange(
                SleepPlan(
                    bedMinutes = (snap.getLong("bedMinutes") ?: d.bedMinutes.toLong()).toInt(),
                    wakeMinutes = (snap.getLong("wakeMinutes") ?: d.wakeMinutes.toLong()).toInt(),
                    windDownMinutes = (snap.getLong("windDownMinutes") ?: d.windDownMinutes.toLong()).toInt()
                )
            )
        }
    }

    fun saveSleep(p: SleepPlan) {
        userDoc()?.collection("settings")?.document("live")
            ?.set(
                mapOf(
                    "bedMinutes" to p.bedMinutes,
                    "wakeMinutes" to p.wakeMinutes,
                    "windDownMinutes" to p.windDownMinutes
                ),
                SetOptions.merge()
            )
    }

    // onChange(makanYangSudah, sebelumTidurOk, bangunTepatWaktuOk)
    fun observeDay(dateKey: String, onChange: (Set<String>, Boolean, Boolean) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val meals = (snap.get("meals") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            onChange(
                meals,
                snap.getBoolean("sleepBed") ?: false,
                snap.getBoolean("sleepWake") ?: false
            )
        }
    }

    fun setMeal(dateKey: String, meal: String, on: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        val op = if (on) FieldValue.arrayUnion(meal) else FieldValue.arrayRemove(meal)
        ref.set(mapOf("meals" to op), SetOptions.merge())
    }

    // field = "sleepBed" atau "sleepWake".
    fun setSleepFlag(dateKey: String, field: String, on: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        ref.set(mapOf(field to on), SetOptions.merge())
    }
}
