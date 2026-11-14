// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/ui/SaveScreen.kt
// Layar Up Save: daftar perangkat elektronik + checklist sebelum berangkat / sebelum tidur.
package com.example.apk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.data.Device
import com.example.apk.data.DeviceRepository
import com.example.apk.data.HabitRepository
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSaveIcon
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack

private val saveDeleteRed = Color(0xFFD32F2F)

// Pasangan (emoji, nama).
private val suggestions = listOf(
    "💡" to "Lampu", "❄️" to "AC", "🌀" to "Kipas", "🔌" to "Charger",
    "💻" to "Laptop", "🚰" to "Dispenser", "🖥️" to "Komputer", "📺" to "TV", "🍚" to "Rice cooker"
)
private val starterPack = listOf(
    "💡" to "Lampu kamar", "🌀" to "AC / Kipas", "🔌" to "Charger HP", "💻" to "Laptop"
)

@Composable
fun SaveScreen(done: Set<String>, todayKey: String, onBack: () -> Unit) {
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var leave by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sleep by remember { mutableStateOf<Set<String>>(emptySet()) }
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 = sebelum berangkat, 1 = sebelum tidur
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Device?>(null) }
    var editing by remember { mutableStateOf<Device?>(null) }
    
    DisposableEffect(todayKey) {
        val l1 = DeviceRepository.observeDevices { devices = it }
        val l2 = DeviceRepository.observeChecks(todayKey) { a, b ->
            leave = a
            sleep = b
        }
        onDispose {
            l1?.remove()
            l2?.remove()
        }
    }

    // Rutinitas Sanitasi di Beranda selesai kalau semua perangkat sudah dicentang.
    fun syncFlags(devs: List<Device>, l: Set<String>, s: Set<String>) {
        val wantLeave = devs.isNotEmpty() && devs.all { it.id in l }
        val wantSleep = devs.isNotEmpty() && devs.all { it.id in s }
        if (wantLeave != ("pre_leave" in done)) HabitRepository.setDone(todayKey, "pre_leave", wantLeave)
        if (wantSleep != ("pre_sleep" in done)) HabitRepository.setDone(todayKey, "pre_sleep", wantSleep)
    }

    fun toggle(d: Device) {
        val field = if (mode == 0) "leave" else "sleep"
        val current = if (mode == 0) leave else sleep
        val on = d.id !in current
        DeviceRepository.setChecked(todayKey, field, d.id, on)
        val updated = if (on) current + d.id else current - d.id
        if (mode == 0) syncFlags(devices, updated, sleep) else syncFlags(devices, leave, updated)
    }

    val checkedSet = if (mode == 0) leave else sleep
    val total = devices.size
    val checkedCount = devices.count { it.id in checkedSet }
    val progress by animateFloatAsState(
        if (total == 0) 0f else checkedCount.toFloat() / total,
        tween(500),
        label = "saveProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFCFF2DB), Color(0xFFE9F7EF), LuBackground)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = LuTextDark)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text("Up Save", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Text("Matikan dulu, baru tenang ⚡", fontSize = 12.sp, color = LuTextGray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ModeSwitch(mode) { mode = it }
                Spacer(Modifier.height(16.dp))
                SaveProgressCard(mode, checkedCount, total, progress)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Perangkat kamu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Text("$total perangkat", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuSaveIcon)
            }
            Spacer(Modifier.height(12.dp))

            if (devices.isEmpty()) {
                EmptyState { DeviceRepository.addMany(starterPack) }
            }
            devices.forEach { d ->
                  DeviceRow(
                    device = d,
                    checked = d.id in checkedSet,
                    onToggle = { toggle(d) },
                    onEdit = { editing = d },
                    onDelete = { toDelete = d }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuSaveIcon)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tambah perangkat", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "🔔 Pengingat otomatis saat jam berangkat dan jam tidur hadir di tahap berikutnya.",
                fontSize = 12.sp,
                color = LuTextGray
            )
            Spacer(Modifier.height(24.dp))
        }
    }

        if (showAdd) {
        DeviceDialog(
            title = "Tambah perangkat",
            onDismiss = { showAdd = false },
            onSave = { name, emoji ->
                DeviceRepository.addDevice(name, emoji)
                // Perangkat baru belum dimatikan, jadi rutinitas Sanitasi kembali belum selesai.
                if ("pre_leave" in done) HabitRepository.setDone(todayKey, "pre_leave", false)
                if ("pre_sleep" in done) HabitRepository.setDone(todayKey, "pre_sleep", false)
                showAdd = false
            }
        )
    }

    editing?.let { d ->
        DeviceDialog(
            title = "Ubah perangkat",
            initialName = d.name,
            initialEmoji = d.emoji,
            onDismiss = { editing = null },
            onSave = { name, emoji ->
                DeviceRepository.updateDevice(d.id, name, emoji)
                editing = null
            }
        )
    }

    toDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Hapus ${d.name}?") },
            text = { Text("Perangkat ini dihapus dari daftar centangmu.") },
            confirmButton = {
                TextButton(onClick = {
                    val remaining = devices.filter { it.id != d.id }
                    DeviceRepository.deleteDevice(d.id)
                    syncFlags(remaining, leave, sleep)
                    toDelete = null
                }) {
                    Text("Hapus", color = saveDeleteRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ModeSwitch(mode: Int, onChange: (Int) -> Unit) {
    val labels = listOf("🚪 Sebelum Berangkat", "🌙 Sebelum Tidur")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(4.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val active = mode == i
            val bg by animateColorAsState(if (active) LuSaveIcon else Color.Transparent, label = "mode")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onChange(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) Color.White else LuTextGray
                )
            }
        }
    }
}

@Composable
private fun SaveProgressCard(mode: Int, checked: Int, total: Int, progress: Float) {
    val allDone = total > 0 && checked == total
    val title = when {
        total == 0 -> "Belum ada perangkat"
        allDone -> if (mode == 0) "Aman! Siap berangkat ✨" else "Aman! Selamat tidur 😴"
        else -> if (mode == 0) "Sebelum keluar, pastikan mati" else "Sebelum tidur, pastikan mati"
    }
    val sub = if (total == 0) {
        "Tambahkan perangkat listrik di kamarmu."
    } else {
        "$checked dari $total perangkat sudah dimatikan"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (allDone) LuSaveBg else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (allDone) "✅" else "⚡", fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(2.dp))
                Text(sub, fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LuSaveIcon,
                    trackColor = LuTrack
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    checked: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bg by animateColorAsState(if (checked) LuSaveBg else LuSurface, label = "rowBg")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (checked) Color.White else LuSaveBg),
                contentAlignment = Alignment.Center
            ) {
                Text(device.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    device.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (checked) LuTextGray else LuTextDark,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    if (checked) "Sudah mati" else "Masih menyala?",
                    fontSize = 12.sp,
                    color = if (checked) LuSaveIcon else LuTextGray
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Ubah ${device.name}",
                    tint = LuTextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus ${device.name}",
                    tint = LuTextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            SaveCheckCircle(checked)
            Spacer(Modifier.width(6.dp))
        }
    }
}
@Composable
private fun SaveCheckCircle(checked: Boolean) {
    val fill by animateColorAsState(if (checked) LuSaveIcon else Color.Transparent, label = "saveCheck")
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, if (checked) LuSaveIcon else LuBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun EmptyState(onStarter: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔌", fontSize = 44.sp)
            Spacer(Modifier.height(8.dp))
            Text("Belum ada perangkat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(4.dp))
            Text(
                "Catat barang elektronik di kamarmu, lalu centang saat sudah dimatikan.",
                fontSize = 13.sp,
                color = LuTextGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStarter,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuSaveIcon)
            ) {
                Text("Tambah paket kamar kos", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text("💡 Lampu · 🌀 AC/Kipas · 🔌 Charger · 💻 Laptop", fontSize = 11.sp, color = LuTextGray)
        }
    }
}

private val emojiPalette = listOf(
    "💡", "❄️", "🌀", "🔌", "💻", "🚰", "🖥️", "📺", "🍚", "📱", "🎧",
    "🔥", "☕", "🧺", "🎮", "📻", "🔦", "🪫", "🛏️", "🧊", "♨️", "🖨️"
)

@Composable
private fun DeviceDialog(
    title: String,
    initialName: String = "",
    initialEmoji: String = "🔌",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var emoji by rememberSaveable { mutableStateOf(initialEmoji) }
    var custom by rememberSaveable {
        mutableStateOf(if (initialEmoji in emojiPalette) "" else initialEmoji)
    }
    val finalEmoji = custom.trim().ifEmpty { emoji }.take(8)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Pilih cepat", fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { (e, n) ->
                        val selected = name == n && emoji == e && custom.isBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) LuSaveIcon else LuSaveBg)
                                .clickable {
                                    name = n
                                    emoji = e
                                    custom = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "$e $n",
                                fontSize = 13.sp,
                                color = if (selected) Color.White else LuTextDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Atau buat sendiri", fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Nama perangkat") },
                    placeholder = { Text("misal: Lampu belajar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text("Pilih ikon", fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                emojiPalette.chunked(6).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { e ->
                            val selected = custom.isBlank() && emoji == e
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) LuSaveIcon else LuSaveBg)
                                    .clickable {
                                        emoji = e
                                        custom = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(e, fontSize = 20.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.take(8) },
                    label = { Text("Atau ketik emoji sendiri") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tampil seperti ini: ", fontSize = 12.sp, color = LuTextGray)
                    Text("$finalEmoji  ${name.ifBlank { "..." }}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name, finalEmoji) },
                enabled = name.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
