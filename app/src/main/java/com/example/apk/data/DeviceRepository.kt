// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/data/DeviceRepository.kt
// Daftar perangkat elektronik milik pengguna + centang harian.
// Struktur: users/{uid}/devices/{id}  dan  users/{uid}/days/{yyyy-MM-dd} (field "leave" dan "sleep")
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
    val emoji: String,
    val order: Long = 0L
)

object DeviceRepository {

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
                Device(d.id, name, d.getString("emoji") ?: "🔌", d.getLong("order") ?: 0L)
            }.sortedBy { it.order }
            onChange(list)
        }
    }

    fun addDevice(name: String, emoji: String) {
        val col = userDoc()?.collection("devices") ?: return
        col.add(
            mapOf(
                "name" to name.trim(),
                "emoji" to emoji,
                "order" to System.currentTimeMillis()
            )
        )
    }

    // items: pasangan (emoji, nama).
    fun addMany(items: List<Pair<String, String>>) {
        val col = userDoc()?.collection("devices") ?: return
        val base = System.currentTimeMillis()
        items.forEachIndexed { i, (emoji, name) ->
            col.add(mapOf("name" to name, "emoji" to emoji, "order" to base + i))
        }
    }

    fun deleteDevice(id: String) {
        userDoc()?.collection("devices")?.document(id)?.delete()
    }

        fun updateDevice(id: String, name: String, emoji: String) {
        userDoc()?.collection("devices")?.document(id)
            ?.update(mapOf("name" to name.trim(), "emoji" to emoji))
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
}
