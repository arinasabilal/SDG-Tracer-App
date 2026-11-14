// LOKASI: app/src/main/java/com/example/apk/data/DeviceRepository.kt (TIMPA)
// Perangkat elektronik, centang harian, dan periode tracker Up Save.
// Struktur: users/{uid}/devices/{id}, users/{uid}/days/{tanggal} (field leave, sleep),
//           users/{uid}/settings/upSave (startDate, durationDays)
package com.example.apk.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

data class Device(
    val id: String,
    val name: String,
    val icon: String,
    val order: Long = 0L
)

// durationDays = 0 berarti tanpa batas waktu.
data class TrackerPlan(val startDate: String, val durationDays: Int)

object DeviceRepository {

    // Perangkat lama menyimpan emoji. Dipetakan ke kunci ikon supaya tetap tampil benar.
    private val legacyEmoji: Map<String, String> = mapOf(
        "💡" to "bulb", "❄" to "ac", "🌀" to "fan", "🔌" to "charger", "💻" to "laptop",
        "🚰" to "dispenser", "🖥" to "computer", "📺" to "tv", "🍚" to "rice", "📱" to "phone",
        "🎧" to "headphone", "🔥" to "heater", "☕" to "coffee", "🧺" to "laundry", "🎮" to "game",
        "🔦" to "flashlight", "🪫" to "charger", "🧊" to "fridge", "♨" to "heater", "🖨" to "print"
    ).mapKeys { it.key.replace("\uFE0F", "") }

    private fun iconFromLegacy(emoji: String?): String {
        val e = emoji?.replace("\uFE0F", "")?.trim().orEmpty()
        return legacyEmoji[e] ?: "other"
    }

    private fun userDoc(): DocumentReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    fun observeDevices(onChange: (List<Device>) -> Unit): ListenerRegistration? {
        val col = userDoc()?.collection("devices") ?: return null
        return col.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val list = snap.documents.mapNotNull { d ->
                val name = d.getString("name") ?: return@mapNotNull null
                val icon = d.getString("icon") ?: iconFromLegacy(d.getString("emoji"))
                Device(d.id, name, icon, d.getLong("order") ?: 0L)
            }.sortedBy { it.order }
            onChange(list)
        }
    }

    fun addDevice(name: String, icon: String) {
        val col = userDoc()?.collection("devices") ?: return
        col.add(
            mapOf(
                "name" to name.trim(),
                "icon" to icon,
                "order" to System.currentTimeMillis()
            )
        )
    }

    // items: pasangan (kunci ikon, nama).
    fun addMany(items: List<Pair<String, String>>) {
        val col = userDoc()?.collection("devices") ?: return
        val base = System.currentTimeMillis()
        items.forEachIndexed { i, (icon, name) ->
            col.add(mapOf("name" to name, "icon" to icon, "order" to base + i))
        }
    }

    fun updateDevice(id: String, name: String, icon: String) {
        userDoc()?.collection("devices")?.document(id)
            ?.update(mapOf("name" to name.trim(), "icon" to icon))
    }

    fun deleteDevice(id: String) {
        userDoc()?.collection("devices")?.document(id)?.delete()
    }

    // Pantau id perangkat yang sudah dimatikan hari ini: (sebelum berangkat, sebelum tidur).
    fun observeChecks(dateKey: String, onChange: (Set<String>, Set<String>) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val leave = (snap.get("leave") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            val sleep = (snap.get("sleep") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            onChange(leave, sleep)
        }
    }

    // field = "leave" atau "sleep".
    fun setChecked(dateKey: String, field: String, deviceId: String, on: Boolean) {
        val ref = userDoc()?.collection("days")?.document(dateKey) ?: return
        val op = if (on) FieldValue.arrayUnion(deviceId) else FieldValue.arrayRemove(deviceId)
        ref.set(mapOf(field to op), SetOptions.merge())
    }

    // Periode tracker. onChange(null) = belum pernah diatur.
    fun observePlan(onChange: (TrackerPlan?) -> Unit): ListenerRegistration? {
        val ref = userDoc()?.collection("settings")?.document("upSave") ?: return null
        return ref.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            if (!snap.exists()) {
                onChange(null)
                return@addSnapshotListener
            }
            onChange(
                TrackerPlan(
                    snap.getString("startDate") ?: HabitRepository.todayKey(),
                    (snap.getLong("durationDays") ?: 0L).toInt()
                )
            )
        }
    }

    fun savePlan(durationDays: Int, startDate: String) {
        userDoc()?.collection("settings")?.document("upSave")
            ?.set(mapOf("startDate" to startDate, "durationDays" to durationDays))
    }
}
