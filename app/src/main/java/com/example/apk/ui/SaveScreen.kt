// LOKASI: app/src/main/java/com/example/apk/ui/SaveScreen.kt (TIMPA)
// Layar Up Save: periode tracker, daftar perangkat, checklist sebelum berangkat / sebelum tidur.
package com.example.apk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.data.Device
import com.example.apk.data.DeviceRepository
import com.example.apk.data.HabitRepository
import com.example.apk.data.TrackerPlan
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSaveIcon
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val SaveRed = Color(0xFFD32F2F)
private val StatusAmber = Color(0xFFB45309)
private val StatusAmberBg = Color(0xFFFFF0CC)
private val StatusGreenBg = Color(0xFFD3F0DE)
private val RowDoneBg = Color(0xFFEAF8EF)

private val quickPicks = listOf(
    "bulb" to "Lampu", "ac" to "AC", "fan" to "Kipas", "charger" to "Charger",
    "laptop" to "Laptop", "dispenser" to "Dispenser", "computer" to "Komputer",
    "tv" to "TV", "rice" to "Rice cooker"
)
private val starterPack = listOf(
    "bulb" to "Lampu kamar", "fan" to "AC / Kipas", "charger" to "Charger HP", "laptop" to "Laptop"
)
private val planOptions = listOf(7, 14, 30, 60, 90, 0)

@Composable
fun SaveScreen(done: Set<String>, todayKey: String, onBack: () -> Unit) {
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var leave by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sleep by remember { mutableStateOf<Set<String>>(emptySet()) }
    var plan by remember { mutableStateOf<TrackerPlan?>(null) }
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 = sebelum berangkat, 1 = sebelum tidur
    var showAdd by remember { mutableStateOf(false) }
    var showPlan by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Device?>(null) }
    var toDelete by remember { mutableStateOf<Device?>(null) }

    DisposableEffect(todayKey) {
        val l1 = DeviceRepository.observeDevices { devices = it }
        val l2 = DeviceRepository.observeChecks(todayKey) { a, b ->
            leave = a
            sleep = b
        }
        val l3 = DeviceRepository.observePlan { plan = it }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
        }
    }

    // Rutinitas Sanitasi di Beranda selesai kalau semua perangkat sudah dimatikan.
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
                        listOf(Color(0xFFBFEBD0), Color(0xFFE3F6EA), LuBackground)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = LuTextDark)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text("Up Save", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Text("Matikan dulu, baru tenang", fontSize = 12.sp, color = LuTextGray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ModeSwitch(mode) { mode = it }
                Spacer(Modifier.height(14.dp))
                SaveProgressCard(mode, checkedCount, total, progress)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            PeriodCard(plan, todayKey) { showPlan = true }

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Perangkat kamu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuSaveBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("$total perangkat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuSaveIcon)
                }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF3AAE72), LuSaveIcon)))
                    .clickable { showAdd = true },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah perangkat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = LuTextGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Pengingat otomatis saat jam berangkat dan jam tidur hadir di tahap berikutnya.",
                    fontSize = 12.sp,
                    color = LuTextGray
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAdd) {
        DeviceDialog(
            title = "Tambah perangkat",
            onDismiss = { showAdd = false },
            onSave = { name, icon ->
                DeviceRepository.addDevice(name, icon)
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
            initialIcon = d.icon,
            onDismiss = { editing = null },
            onSave = { name, icon ->
                DeviceRepository.updateDevice(d.id, name, icon)
                editing = null
            }
        )
    }

    if (showPlan) {
        PlanDialog(
            plan = plan,
            todayKey = todayKey,
            onDismiss = { showPlan = false },
            onSave = { days, start ->
                DeviceRepository.savePlan(days, start)
                showPlan = false
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
                    Text("Hapus", color = SaveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Batal") }
            }
        )
    }
}

// ---------------------------------------------------------------- KOMPONEN

@Composable
private fun ModeSwitch(mode: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(4.dp)
    ) {
        listOf("Sebelum Berangkat", "Sebelum Tidur").forEachIndexed { i, label ->
            val active = mode == i
            val bg by animateColorAsState(if (active) LuSaveIcon else Color.Transparent, label = "mode")
            val fg = if (active) Color.White else LuTextGray
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onChange(i) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (i == 0) Icons.AutoMirrored.Filled.ExitToApp else Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
            }
        }
    }
}

@Composable
private fun SaveProgressCard(mode: Int, checked: Int, total: Int, progress: Float) {
    val allDone = total > 0 && checked == total
    val title = when {
        total == 0 -> "Belum ada perangkat"
        allDone -> if (mode == 0) "Aman! Siap berangkat" else "Aman! Selamat tidur"
        else -> if (mode == 0) "Sebelum keluar, pastikan mati" else "Sebelum tidur, pastikan mati"
    }
    val sub = if (total == 0) {
        "Tambahkan perangkat listrik di kamarmu."
    } else {
        "$checked dari $total perangkat sudah dimatikan"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 11.dp.toPx()
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = LuTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    if (progress > 0f) {
                        drawArc(
                            color = LuSaveIcon,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Text("$checked/$total", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(2.dp))
                Text(sub, fontSize = 12.sp, color = LuTextGray)
            }
        }
    }
}

@Composable
private fun PeriodCard(plan: TrackerPlan?, todayKey: String, onEdit: () -> Unit) {
    val total = plan?.durationDays ?: 0
    val dayNo = if (plan == null) 0 else (daysBetween(plan.startDate, todayKey) + 1).coerceAtLeast(1)
    val finished = plan != null && total > 0 && dayNo > total
    val (title, sub) = periodTexts(plan, dayNo)
    val progress by animateFloatAsState(
        if (plan != null && total > 0) dayNo.coerceAtMost(total).toFloat() / total else 0f,
        tween(500),
        label = "period"
    )
    val action = when {
        plan == null -> "Atur"
        finished -> "Mulai baru"
        else -> "Ubah"
    }
    val blue = Color(0xFF3B82F6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF8BD3FF), blue))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Periode tracker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LuTextGray)
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Text(sub, fontSize = 12.sp, color = LuTextGray)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuSaveBg)
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuSaveIcon)
                }
            }
            if (plan != null && total > 0) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (finished) LuSaveIcon else blue,
                    trackColor = LuTrack
                )
            }
        }
    }
}

@Composable
private fun DeviceBadge(icon: DeviceIcon, size: Dp, dimmed: Boolean = false, showCheck: Boolean = false) {
    val alpha by animateFloatAsState(if (dimmed) 0.55f else 1f, label = "badgeAlpha")
    Box(Modifier.size(size + 6.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(size)
                .alpha(alpha)
                .clip(RoundedCornerShape(size * 0.32f))
                .background(Brush.linearGradient(listOf(icon.from, icon.to))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon.vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
        }
        if (showCheck) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(LuSaveIcon),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun StatusPill(off: Boolean) {
    val color = if (off) LuSaveIcon else StatusAmber
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (off) StatusGreenBg else StatusAmberBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            if (off) "Sudah mati" else "Masih menyala",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun PowerButton(off: Boolean) {
    val fill by animateColorAsState(if (off) LuSaveIcon else Color.Transparent, label = "power")
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, if (off) LuSaveIcon else LuBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (off) Icons.Default.Check else Icons.Default.PowerSettingsNew,
            contentDescription = null,
            tint = if (off) Color.White else LuTextGray,
            modifier = Modifier.size(20.dp)
        )
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
    val bg by animateColorAsState(if (checked) RowDoneBg else LuSurface, label = "rowBg")
    val icon = deviceIconFor(device.icon)
    var menu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceBadge(icon, 52.dp, dimmed = checked, showCheck = checked)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    device.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (checked) LuTextGray else LuTextDark,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(Modifier.height(4.dp))
                StatusPill(checked)
            }
            PowerButton(checked)
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu ${device.name}",
                        tint = LuTextGray
                    )
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Ubah") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = SaveRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SaveRed) },
                        onClick = {
                            menu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onStarter: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DeviceBadge(deviceIconFor("other"), 72.dp)
            Spacer(Modifier.height(14.dp))
            Text("Belum ada perangkat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(4.dp))
            Text(
                "Catat barang elektronik di kamarmu, lalu tandai saat sudah dimatikan.",
                fontSize = 13.sp,
                color = LuTextGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                starterPack.forEach { (key, _) -> DeviceBadge(deviceIconFor(key), 40.dp) }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStarter,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuSaveIcon)
            ) {
                Text("Tambah paket kamar kos", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------- DIALOG

@Composable
private fun DeviceDialog(
    title: String,
    initialName: String = "",
    initialIcon: String = "charger",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var iconKey by rememberSaveable { mutableStateOf(initialIcon) }

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
                    quickPicks.forEach { (key, label) ->
                        val ic = deviceIconFor(key)
                        val selected = name == label && iconKey == key
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) LuSaveIcon else LuSaveBg)
                                .clickable {
                                    name = label
                                    iconKey = key
                                }
                                .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(ic.from, ic.to))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(ic.vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(label, fontSize = 13.sp, color = if (selected) Color.White else LuTextDark)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
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
                deviceIcons.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { ic ->
                            val selected = ic.key == iconKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(ic.from, ic.to)))
                                    .then(
                                        if (selected) Modifier.border(3.dp, LuTextDark, RoundedCornerShape(14.dp))
                                        else Modifier
                                    )
                                    .clickable { iconKey = ic.key },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(ic.vector, contentDescription = ic.label, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DeviceBadge(deviceIconFor(iconKey), 40.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        name.ifBlank { "Nama perangkat" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (name.isBlank()) LuTextGray else LuTextDark
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name, iconKey) },
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

@Composable
private fun PlanDialog(
    plan: TrackerPlan?,
    todayKey: String,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    val finishedNow = plan != null && plan.durationDays > 0 &&
        daysBetween(plan.startDate, todayKey) + 1 > plan.durationDays
    var selected by rememberSaveable { mutableIntStateOf(plan?.durationDays ?: 30) }
    var custom by rememberSaveable {
        mutableStateOf(
            plan?.durationDays?.takeIf { it > 0 && it !in planOptions }?.toString() ?: ""
        )
    }
    var restart by rememberSaveable { mutableStateOf(plan == null || finishedNow) }

    val days = custom.toIntOrNull()?.coerceIn(1, 365) ?: selected
    val startKey = if (plan == null || restart) todayKey else plan.startDate
    val endText = if (days == 0) {
        "Tanpa batas waktu"
    } else {
        "Selesai pada ${prettyDate(addDaysKey(startKey, days - 1))}"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur periode tracker") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Berapa hari kamu mau menjaga kebiasaan ini?", fontSize = 13.sp, color = LuTextGray)
                Spacer(Modifier.height(12.dp))
                planOptions.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { d ->
                            val active = custom.isBlank() && selected == d
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (active) LuSaveIcon else LuSaveBg)
                                    .clickable {
                                        selected = d
                                        custom = ""
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (d == 0) "Tanpa batas" else "$d hari",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) Color.White else LuTextDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Atau jumlah hari sendiri") },
                    placeholder = { Text("misal: 45") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (plan != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Hitung ulang dari hari ini",
                            fontSize = 14.sp,
                            color = LuTextDark,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = restart, onCheckedChange = { restart = it })
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(endText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuSaveIcon)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(days, startKey) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ---------------------------------------------------------------- TANGGAL

private fun parseKey(key: String): Long? =
    runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)?.time }.getOrNull()

private fun daysBetween(fromKey: String, toKey: String): Int {
    val a = parseKey(fromKey) ?: return 0
    val b = parseKey(toKey) ?: return 0
    return ((b - a) / 86_400_000.0).roundToInt()
}

private fun addDaysKey(key: String, days: Int): String {
    val t = parseKey(key) ?: return key
    val cal = Calendar.getInstance()
    cal.timeInMillis = t
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

private fun prettyDate(key: String): String {
    val t = parseKey(key) ?: return key
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(t))
}

private fun periodTexts(plan: TrackerPlan?, dayNo: Int): Pair<String, String> {
    if (plan == null) {
        return "Atur periode tracker" to "Tentukan berapa hari kamu mau menjaga kebiasaan ini."
    }
    val total = plan.durationDays
    if (total == 0) {
        return "Hari ke-$dayNo" to "Tanpa batas waktu · mulai ${prettyDate(plan.startDate)}"
    }
    if (dayNo > total) {
        return "Periode selesai" to "$total hari sudah terlewati. Mulai periode baru?"
    }
    val left = total - dayNo
    val head = if (left == 0) "Hari terakhir" else "Sisa $left hari"
    return "Hari ke-$dayNo dari $total" to "$head · selesai ${prettyDate(addDaysKey(plan.startDate, total - 1))}"
}
