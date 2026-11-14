// LOKASI: app/src/main/java/com/example/apk/ui/DeviceIcons.kt (file BARU)
// Katalog ikon perangkat: ikon vektor + warna gradien (pengganti emoji).
package com.example.apk.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class DeviceIcon(
    val key: String,
    val label: String,
    val vector: ImageVector,
    val from: Color,
    val to: Color
)

val deviceIcons: List<DeviceIcon> = listOf(
    DeviceIcon("bulb", "Lampu", Icons.Filled.Lightbulb, Color(0xFFFFD166), Color(0xFFFF9F1C)),
    DeviceIcon("ac", "AC", Icons.Filled.AcUnit, Color(0xFF7DD3FC), Color(0xFF3B82F6)),
    DeviceIcon("fan", "Kipas", Icons.Filled.Air, Color(0xFF5EEAD4), Color(0xFF0D9488)),
    DeviceIcon("charger", "Charger", Icons.Filled.Power, Color(0xFF86EFAC), Color(0xFF16A34A)),
    DeviceIcon("phone", "HP", Icons.Filled.Smartphone, Color(0xFFC4B5FD), Color(0xFF7C3AED)),
    DeviceIcon("laptop", "Laptop", Icons.Filled.Laptop, Color(0xFFA5B4FC), Color(0xFF4F46E5)),
    DeviceIcon("computer", "Komputer", Icons.Filled.Computer, Color(0xFF93C5FD), Color(0xFF2563EB)),
    DeviceIcon("tv", "TV", Icons.Filled.Tv, Color(0xFFF9A8D4), Color(0xFFDB2777)),
    DeviceIcon("dispenser", "Dispenser", Icons.Filled.WaterDrop, Color(0xFF67E8F9), Color(0xFF0891B2)),
    DeviceIcon("rice", "Rice cooker", Icons.Filled.RiceBowl, Color(0xFFFDBA74), Color(0xFFEA580C)),
    DeviceIcon("microwave", "Microwave", Icons.Filled.Microwave, Color(0xFFFCA5A5), Color(0xFFDC2626)),
    DeviceIcon("fridge", "Kulkas", Icons.Filled.Kitchen, Color(0xFFBAE6FD), Color(0xFF0284C7)),
    DeviceIcon("heater", "Pemanas", Icons.Filled.LocalFireDepartment, Color(0xFFFDA4AF), Color(0xFFE11D48)),
    DeviceIcon("coffee", "Teko listrik", Icons.Filled.Coffee, Color(0xFFD6B28F), Color(0xFF92400E)),
    DeviceIcon("laundry", "Mesin cuci", Icons.Filled.LocalLaundryService, Color(0xFF99F6E4), Color(0xFF0F766E)),
    DeviceIcon("game", "Konsol game", Icons.Filled.SportsEsports, Color(0xFFD8B4FE), Color(0xFF9333EA)),
    DeviceIcon("headphone", "Headphone", Icons.Filled.Headphones, Color(0xFFFDA4D4), Color(0xFFBE185D)),
    DeviceIcon("speaker", "Speaker", Icons.Filled.Speaker, Color(0xFFCBD5E1), Color(0xFF475569)),
    DeviceIcon("router", "Router", Icons.Filled.Router, Color(0xFF6EE7B7), Color(0xFF059669)),
    DeviceIcon("print", "Printer", Icons.Filled.Print, Color(0xFFE2E8F0), Color(0xFF64748B)),
    DeviceIcon("flashlight", "Senter", Icons.Filled.FlashlightOn, Color(0xFFFDE68A), Color(0xFFCA8A04)),
    DeviceIcon("other", "Lainnya", Icons.Filled.ElectricalServices, Color(0xFFBBF7D0), Color(0xFF15803D))
)

fun deviceIconFor(key: String): DeviceIcon =
    deviceIcons.firstOrNull { it.key == key } ?: deviceIcons.last()
