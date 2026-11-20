// LOKASI: app/src/main/java/com/example/apk/ui/TaskScreen.kt (file BARU)
// Layar Up Task: timer fokus (Pomodoro) + daftar tugas dengan prioritas dan tenggat.
package com.example.apk.ui

import android.os.SystemClock
import com.example.apk.notif.ReminderScheduler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.data.FocusSettings
import com.example.apk.data.HabitRepository
import com.example.apk.data.TaskItem
import com.example.apk.data.TaskRepository
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTaskBg
import com.example.apk.ui.theme.LuTaskIcon
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val TkRed = Color(0xFFD32F2F)
private val TkAmber = Color(0xFFF59E0B)
private val TkAmberDark = Color(0xFFB45309)
private val TkGreen = Color(0xFF27804D)

// ---------------------------------------------------------------- TIMER

// Disimpan di luar layar supaya timer tetap berjalan saat kamu pindah layar di dalam aplikasi.
// Kalau aplikasinya ditutup total, timer ikut berhenti.
object FocusTimer {
    var running by mutableStateOf(false)
        private set
    var isBreak by mutableStateOf(false)
        private set
    var remainingSec by mutableIntStateOf(25 * 60)
        private set
    var focusMin by mutableIntStateOf(25)
        private set
    var breakMin by mutableIntStateOf(5)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    // Total menit fokus hari ini, diisi dari layar Up Task.
    var todayMinutes: Int = 0

    private var endAt = 0L
    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val totalSec: Int
        get() = (if (isBreak) breakMin else focusMin) * 60

    fun applySettings(focus: Int, brk: Int) {
        val untouched = remainingSec == totalSec
        focusMin = focus
        breakMin = brk
        if (!running && untouched) remainingSec = totalSec
    }

    fun start() {
        if (running) return
        message = null
        endAt = SystemClock.elapsedRealtime() + remainingSec * 1000L
        running = true
        ReminderScheduler.appContext?.let {
            val doneTitle = if (isBreak) "Istirahat selesai" else "Sesi fokus selesai"
            val doneText = if (isBreak) {
                "Siap fokus lagi? Buka Up Task untuk mulai."
            } else {
                "Kerja bagus! Waktunya istirahat $breakMin menit."
            }
            ReminderScheduler.scheduleTimer(it, remainingSec, doneTitle, doneText)
        }
        job?.cancel()
        job = scope.launch {
            while (running) {
                val left = ((endAt - SystemClock.elapsedRealtime() + 999) / 1000).toInt()
                if (left <= 0) {
                    finish()
                    break
                }
                remainingSec = left
                delay(250)
            }
        }
    }

    fun pause() {
        running = false
        job?.cancel()
        ReminderScheduler.appContext?.let { ReminderScheduler.cancelTimer(it) }
    }

    fun reset() {
        job?.cancel()
        ReminderScheduler.appContext?.let { ReminderScheduler.cancelTimer(it) }
        running = false
        isBreak = false
        remainingSec = focusMin * 60
        message = null
    }

    private fun finish() {
        running = false
        if (!isBreak) {
            val key = HabitRepository.todayKey()
            TaskRepository.addFocusSession(key, focusMin)
            todayMinutes += focusMin
            if (todayMinutes >= 25) HabitRepository.setDone(key, "focus", true)
            isBreak = true
            remainingSec = breakMin * 60
            message = "Sesi fokus selesai! Waktunya istirahat $breakMin menit."
        } else {
            isBreak = false
            remainingSec = focusMin * 60
            message = "Istirahat selesai. Siap fokus lagi?"
        }
    }
}

// ---------------------------------------------------------------- LAYAR

@Composable
fun TaskScreen(done: Set<String>, todayKey: String, onBack: () -> Unit) {
    var tasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }
    var tasksLoaded by remember { mutableStateOf(false) }
    var focusToday by remember { mutableIntStateOf(0) }
    var sessionsToday by remember { mutableIntStateOf(0) }
    var focusLoaded by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<TaskItem?>(null) }
    val doneNow by rememberUpdatedState(done)

    DisposableEffect(todayKey) {
        val l1 = TaskRepository.observeTasks {
            tasks = it
            tasksLoaded = true
        }
        val l2 = TaskRepository.observeFocusToday(todayKey) { minutes, sessions ->
            focusToday = minutes
            sessionsToday = sessions
            focusLoaded = true
            FocusTimer.todayMinutes = minutes
        }
        val l3 = TaskRepository.observeFocusSettings { f ->
            FocusTimer.applySettings(f.focusMinutes, f.breakMinutes)
        }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
        }
    }

    // Rutinitas "tasks" di Beranda: selesai kalau ada tugas dan tidak ada yang belum selesai
    // dengan tenggat hari ini atau yang sudah lewat.
    LaunchedEffect(tasks, tasksLoaded, todayKey) {
        if (!tasksLoaded) return@LaunchedEffect
        val want = tasks.isNotEmpty() &&
            tasks.none { !it.done && it.deadline.isNotEmpty() && it.deadline <= todayKey }
        if (want != ("tasks" in doneNow)) HabitRepository.setDone(todayKey, "tasks", want)
    }

    // Rutinitas "focus": selesai setelah total 25 menit fokus hari ini.
    LaunchedEffect(focusToday, focusLoaded, todayKey) {
        if (focusLoaded && focusToday >= 25 && "focus" !in doneNow) {
            HabitRepository.setDone(todayKey, "focus", true)
        }
    }

    val open = tasks.filter { !it.done }.sortedWith(
        compareBy<TaskItem>(
            { if (it.deadline.isEmpty()) "9999-99-99" else it.deadline },
            { it.priority },
            { it.createdAt }
        )
    )
    val finished = tasks.filter { it.done }.sortedByDescending { it.createdAt }
    val urgent = open.count { it.deadline.isNotEmpty() && it.deadline <= todayKey }

    val focusOk = "focus" in done
    val tasksOk = "tasks" in done

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
                        listOf(Color(0xFFFFE9B0), Color(0xFFFFF4D6), LuBackground)
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
                        Text("Up Task", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Text("Fokus dulu, tugas beres", fontSize = 12.sp, color = LuTextGray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                TkHeaderCard(focusOk, tasksOk, open.size, urgent)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            TkTimerCard(focusToday, sessionsToday)

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tugas dan tenggat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuTaskBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("${open.size} aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuTaskIcon)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                TkEmptyCard()
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LuSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        open.forEach { t ->
                            TkTaskRow(
                                task = t,
                                todayKey = todayKey,
                                onToggle = { TaskRepository.setTaskDone(t.id, true) },
                                onDelete = { toDelete = t }
                            )
                        }
                        if (open.isEmpty()) {
                            Text(
                                "Semua tugas selesai. Mantap!",
                                fontSize = 13.sp,
                                color = LuTextGray,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        if (finished.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            HorizontalDivider(color = LuBorder)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Selesai (${finished.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LuTextGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            finished.forEach { t ->
                                TkTaskRow(
                                    task = t,
                                    todayKey = todayKey,
                                    onToggle = { TaskRepository.setTaskDone(t.id, false) },
                                    onDelete = { toDelete = t }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE0A93B), LuTaskIcon)))
                    .clickable { showAdd = true },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah tugas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    "Pengingat tenggat dan bunyi saat timer selesai hadir di tahap notifikasi.",
                    fontSize = 12.sp,
                    color = LuTextGray
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAdd) {
        TkAddDialog(
            todayKey = todayKey,
            onDismiss = { showAdd = false },
            onSave = { title, deadline, priority ->
                TaskRepository.addTask(title, deadline, priority)
                showAdd = false
            }
        )
    }

    toDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Hapus tugas?") },
            text = { Text("\"${t.title}\" akan dihapus dari daftarmu.") },
            confirmButton = {
                TextButton(onClick = {
                    TaskRepository.deleteTask(t.id)
                    toDelete = null
                }) {
                    Text("Hapus", color = TkRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Batal") }
            }
        )
    }
}

// ---------------------------------------------------------------- KARTU

@Composable
private fun TkHeaderCard(focusOk: Boolean, tasksOk: Boolean, active: Int, urgent: Int) {
    val doneCount = listOf(focusOk, tasksOk).count { it }
    val progress by animateFloatAsState(doneCount / 2f, tween(500), label = "tkRing")
    val message = when (doneCount) {
        0 -> "Mulai dengan satu sesi fokus."
        1 -> "Bagus, tinggal satu lagi."
        else -> "Hari belajar yang produktif!"
    }
    val sub = if (urgent > 0) "$active tugas aktif, $urgent mendesak" else "$active tugas aktif"

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
                            color = LuTaskIcon,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Text("$doneCount/2", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Belajar hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuTaskIcon)
                Spacer(Modifier.height(2.dp))
                Text(message, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(2.dp))
                Text(sub, fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TkChip("Fokus", focusOk)
                    TkChip("Tugas", tasksOk)
                }
            }
        }
    }
}

@Composable
private fun TkChip(label: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (ok) LuTaskIcon else LuTaskBg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ok) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (ok) Color.White else LuTextGray
        )
    }
}

@Composable
private fun TkTimerCard(focusToday: Int, sessions: Int) {
    val isBreak = FocusTimer.isBreak
    val running = FocusTimer.running
    val remaining = FocusTimer.remainingSec
    val total = FocusTimer.totalSec.coerceAtLeast(1)
    val accent = if (isBreak) TkGreen else LuTaskIcon
    val progress by animateFloatAsState(
        1f - remaining.toFloat() / total,
        tween(300),
        label = "tkTimer"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TkBadge(Icons.Default.Timer, Color(0xFFFDE68A), accent, 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Timer fokus", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Text("Teknik Pomodoro", fontSize = 12.sp, color = LuTextGray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isBreak) "Istirahat" else "Fokus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
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
                            color = accent,
                            startAngle = -90f,
                            sweepAngle = 360f * progress.coerceAtMost(1f),
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        tkClock(remaining),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuTextDark
                    )
                    Text(
                        if (running) "sedang berjalan" else "siap dimulai",
                        fontSize = 12.sp,
                        color = LuTextGray
                    )
                }
            }

            FocusTimer.message?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (running) FocusTimer.pause() else FocusTimer.start() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Icon(
                        if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            running -> "Jeda"
                            remaining < total -> "Lanjut"
                            else -> "Mulai"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = { FocusTimer.reset() },
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.5.dp, accent)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ulang dari awal", tint = accent)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = LuBorder)
            Spacer(Modifier.height(12.dp))
            TkStepRow(
                label = "Durasi fokus",
                value = "${FocusTimer.focusMin} menit",
                enabled = !running,
                onMinus = {
                    val v = (FocusTimer.focusMin - 5).coerceAtLeast(5)
                    FocusTimer.applySettings(v, FocusTimer.breakMin)
                    TaskRepository.saveFocusSettings(FocusSettings(v, FocusTimer.breakMin))
                },
                onPlus = {
                    val v = (FocusTimer.focusMin + 5).coerceAtMost(90)
                    FocusTimer.applySettings(v, FocusTimer.breakMin)
                    TaskRepository.saveFocusSettings(FocusSettings(v, FocusTimer.breakMin))
                }
            )
            Spacer(Modifier.height(10.dp))
            TkStepRow(
                label = "Durasi istirahat",
                value = "${FocusTimer.breakMin} menit",
                enabled = !running,
                onMinus = {
                    val v = (FocusTimer.breakMin - 1).coerceAtLeast(1)
                    FocusTimer.applySettings(FocusTimer.focusMin, v)
                    TaskRepository.saveFocusSettings(FocusSettings(FocusTimer.focusMin, v))
                },
                onPlus = {
                    val v = (FocusTimer.breakMin + 1).coerceAtMost(30)
                    FocusTimer.applySettings(FocusTimer.focusMin, v)
                    TaskRepository.saveFocusSettings(FocusSettings(FocusTimer.focusMin, v))
                }
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Hari ini: $sessions sesi, $focusToday menit fokus",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuTextDark
            )
            Text(
                "Rutinitas Fokus selesai setelah total 25 menit.",
                fontSize = 12.sp,
                color = LuTextGray
            )
        }
    }
}

@Composable
private fun TkStepRow(
    label: String,
    value: String,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = LuTextGray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        }
        TkStep(Icons.Default.Remove, "Kurangi $label", enabled, onMinus)
        Spacer(Modifier.width(8.dp))
        TkStep(Icons.Default.Add, "Tambah $label", enabled, onPlus)
    }
}

@Composable
private fun TkStep(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .border(1.5.dp, LuTaskIcon, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = LuTaskIcon, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TkEmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TkBadge(Icons.Default.Create, Color(0xFFFDE68A), LuTaskIcon, 72.dp)
            Spacer(Modifier.height(14.dp))
            Text("Belum ada tugas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(4.dp))
            Text(
                "Catat tugas kuliah atau sekolahmu beserta tenggatnya, supaya tidak ada yang terlewat.",
                fontSize = 13.sp,
                color = LuTextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TkTaskRow(task: TaskItem, todayKey: String, onToggle: () -> Unit, onDelete: () -> Unit) {
    val pc = tkPriorityColor(task.priority)
    val (label, fg) = tkDeadlineLabel(task.deadline, todayKey)
    val shownFg = if (task.done) LuTextGray else fg
    val bg by animateColorAsState(
        if (task.done) Color(0xFFF3F6F8) else Color.Transparent,
        label = "tkRowBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onToggle)
            .padding(start = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TkCheck(task.done, pc)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (task.done) LuTextGray else LuTextDark,
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(shownFg.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = shownFg)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(pc)
                )
                Spacer(Modifier.width(4.dp))
                Text(tkPriorityLabel(task.priority), fontSize = 11.sp, color = LuTextGray)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Hapus ${task.title}",
                tint = LuTextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TkCheck(checked: Boolean, tint: Color) {
    val fill by animateColorAsState(if (checked) tint else Color.Transparent, label = "tkCheck")
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, tint, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun TkBadge(icon: ImageVector, from: Color, to: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(Brush.linearGradient(listOf(from, to))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}

// ---------------------------------------------------------------- DIALOG TAMBAH TUGAS

private val tkDeadlineOptions = listOf(
    -1 to "Tanpa tenggat", 0 to "Hari ini", 1 to "Besok", 2 to "Lusa",
    3 to "3 hari", 7 to "1 minggu", 14 to "2 minggu"
)

@Composable
private fun TkAddDialog(
    todayKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableIntStateOf(1) }
    var offset by rememberSaveable { mutableIntStateOf(1) }
    val deadlineKey = if (offset < 0) "" else tkAddDays(todayKey, offset)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah tugas") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(60) },
                    label = { Text("Nama tugas") },
                    placeholder = { Text("misal: Laporan praktikum") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("Prioritas", fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 1, 2).forEach { p ->
                        val active = priority == p
                        val c = tkPriorityColor(p)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) c else c.copy(alpha = 0.14f))
                                .clickable { priority = p }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tkPriorityLabel(p),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) Color.White else LuTextDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Tenggat", fontSize = 12.sp, color = LuTextGray)
                Spacer(Modifier.height(8.dp))
                tkDeadlineOptions.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (off, label) ->
                            val active = offset == off
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (active) LuTaskIcon else LuTaskBg)
                                    .clickable { offset = off }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) Color.White else LuTextDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    if (deadlineKey.isEmpty()) "Tanpa tenggat" else "Tenggat: ${tkPretty(deadlineKey)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuTaskIcon
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onSave(title, deadlineKey, priority) },
                enabled = title.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ---------------------------------------------------------------- HELPER

private fun tkClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", s / 60, s % 60)
}

private fun tkPriorityLabel(p: Int): String = when (p) {
    0 -> "Tinggi"
    2 -> "Rendah"
    else -> "Sedang"
}

private fun tkPriorityColor(p: Int): Color = when (p) {
    0 -> TkRed
    2 -> TkGreen
    else -> TkAmber
}

private fun tkDeadlineLabel(deadline: String, todayKey: String): Pair<String, Color> {
    if (deadline.isEmpty()) return "Tanpa tenggat" to LuTextGray
    val d = tkDaysBetween(todayKey, deadline)
    return when {
        d < 0 -> "Terlambat ${-d} hari" to TkRed
        d == 0 -> "Hari ini" to TkAmberDark
        d == 1 -> "Besok" to TkAmberDark
        else -> tkPretty(deadline) to LuTextGray
    }
}

private fun tkParse(key: String): Long? =
    runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)?.time }.getOrNull()

private fun tkDaysBetween(fromKey: String, toKey: String): Int {
    val a = tkParse(fromKey) ?: return 0
    val b = tkParse(toKey) ?: return 0
    return ((b - a) / 86_400_000.0).roundToInt()
}

private fun tkAddDays(key: String, days: Int): String {
    val t = tkParse(key) ?: return key
    val cal = Calendar.getInstance()
    cal.timeInMillis = t
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

private fun tkPretty(key: String): String {
    val t = tkParse(key) ?: return key
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(t))
}
