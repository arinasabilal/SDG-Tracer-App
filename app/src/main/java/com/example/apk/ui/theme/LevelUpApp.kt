// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/ui/LevelUpApp.kt
// Pengatur layar utama. Beranda = ringkasan + riwayat. Centang ada di layar tiap fitur.
package com.example.apk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.auth.AuthManager
import com.example.apk.data.Habit
import com.example.apk.data.HabitModule
import com.example.apk.data.HabitRepository
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBookBg
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSaveIcon
import com.example.apk.ui.theme.LuSkySoft
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTaskBg
import com.example.apk.ui.theme.LuTaskIcon
import com.example.apk.ui.theme.LuTeal
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val LogoutRed = Color(0xFFD32F2F)

enum class Stage { SPLASH, LOGIN, REGISTER, FORGOT, MAIN }

@Composable
fun LevelUpApp() {
    var stage by rememberSaveable { mutableStateOf(Stage.SPLASH) }

    BackHandler(enabled = stage == Stage.REGISTER || stage == Stage.FORGOT) {
        stage = Stage.LOGIN
    }

    Crossfade(targetState = stage, animationSpec = tween(300), label = "stage") { current ->
        when (current) {
            Stage.SPLASH -> SplashScreen(
                onFinished = { stage = if (AuthManager.isLoggedIn) Stage.MAIN else Stage.LOGIN }
            )
            Stage.LOGIN -> LoginScreen(
                onLogin = { stage = Stage.MAIN },
                onGoRegister = { stage = Stage.REGISTER },
                onGoForgot = { stage = Stage.FORGOT }
            )
            Stage.REGISTER -> RegisterScreen(
                onRegistered = { stage = Stage.MAIN },
                onBackToLogin = { stage = Stage.LOGIN }
            )
            Stage.FORGOT -> ForgotPasswordScreen(
                onBack = { stage = Stage.LOGIN }
            )
            Stage.MAIN -> MainShell(
                onLogout = {
                    AuthManager.signOut()
                    stage = Stage.LOGIN
                }
            )
        }
    }
}

private data class NavTab(val label: String, val icon: ImageVector)

// Indeks layar fitur: 0 = Up Live (Kesehatan), 1 = Up Save (Sanitasi), 2 = Up Task (Pendidikan), 3 = Manual Book.
@Composable
fun MainShell(onLogout: () -> Unit = {}) {
    val tabs = listOf(
        NavTab("Beranda", Icons.Default.Home),
        NavTab("Jelajah", Icons.Default.Search),
        NavTab("Progres", Icons.Default.DateRange),
        NavTab("Profil", Icons.Default.Person)
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var openIdx by rememberSaveable { mutableIntStateOf(-1) }
    val userName = remember { AuthManager.displayName }
    val userEmail = remember { AuthManager.email }

    val todayKey = remember { HabitRepository.todayKey() }
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var done by remember { mutableStateOf<Set<String>>(emptySet()) }
    var history by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }

    DisposableEffect(Unit) {
        HabitRepository.seedDefaultsIfEmpty()
        val l1 = HabitRepository.observeHabits { habits = it }
        val l2 = HabitRepository.observeDone(todayKey) { done = it }
        val l3 = HabitRepository.observeHistory(7) { history = it }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
        }
    }

    BackHandler(enabled = openIdx >= 0) { openIdx = -1 }

    Scaffold(
        containerColor = LuBackground,
        bottomBar = {
            BottomBar(tabs, selected) {
                selected = it
                openIdx = -1
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(bottom = inner.calculateBottomPadding())
                .fillMaxSize()
        ) {
            when {
                selected == 0 && openIdx in 0..2 -> ModuleScreen(
                    module = HabitModule.entries[openIdx],
                    habits = habits,
                    done = done,
                    onBack = { openIdx = -1 },
                    onToggle = { id, value -> HabitRepository.setDone(todayKey, id, value) }
                )
                selected == 0 && openIdx == 3 -> ManualBookScreen(onBack = { openIdx = -1 })
                selected == 0 -> HomeScreen(
                    userName = userName,
                    habits = habits,
                    done = done,
                    history = history,
                    todayKey = todayKey,
                    onOpen = { openIdx = it }
                )
                selected == 3 -> Box(Modifier.fillMaxSize().statusBarsPadding()) {
                    ProfileScreen(userName, userEmail, onLogout)
                }
                else -> Box(Modifier.fillMaxSize().statusBarsPadding()) {
                    PlaceholderScreen(tabs[selected].label)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(tabs: List<NavTab>, selected: Int, onSelect: (Int) -> Unit) {
    Surface(color = LuSurface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val active = selected == index
                val tint = if (active) LuTeal else LuTextGray
                val pill by animateColorAsState(
                    if (active) LuTeal.copy(alpha = 0.14f) else Color.Transparent,
                    label = "pill"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(pill),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = tint,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name (segera dibuat)", color = LuTextGray)
    }
}

@Composable
fun ProfileScreen(name: String, email: String, onLogout: () -> Unit) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LuSkySoft)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LuPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Spacer(Modifier.height(2.dp))
                    Text(email, fontSize = 13.sp, color = LuTextGray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { showConfirm = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LogoutRed)
        ) {
            Text("Keluar", fontWeight = FontWeight.Bold, color = LogoutRed)
        }
        Spacer(Modifier.height(12.dp))
        Text("Level Up · Versi 1.0", fontSize = 12.sp, color = LuTextGray)
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Keluar dari akun?") },
            text = { Text("Kamu perlu masuk lagi untuk memakai Level Up.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onLogout()
                }) {
                    Text("Keluar", color = LogoutRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ---------------------------------------------------------------- HELPER MODUL

private fun moduleName(m: HabitModule) = when (m) {
    HabitModule.HEALTH -> "Up Live"
    HabitModule.SANITATION -> "Up Save"
    HabitModule.EDUCATION -> "Up Task"
}

private fun moduleColors(m: HabitModule): Pair<Color, Color> = when (m) {
    HabitModule.HEALTH -> LuLiveBg to LuLiveIcon
    HabitModule.SANITATION -> LuSaveBg to LuSaveIcon
    HabitModule.EDUCATION -> LuTaskBg to LuTaskIcon
}

@Composable
private fun ModuleIcon(module: HabitModule, boxSize: Dp) {
    val (bg, fg) = moduleColors(module)
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(boxSize * 0.3f))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        when (module) {
            HabitModule.HEALTH -> Icon(Icons.Default.Favorite, contentDescription = null, tint = fg)
            HabitModule.SANITATION -> Text("⚡", fontSize = 20.sp)
            HabitModule.EDUCATION -> Icon(Icons.Default.Create, contentDescription = null, tint = fg)
        }
    }
}

// ---------------------------------------------------------------- BERANDA

@Composable
fun HomeScreen(
    userName: String,
    habits: List<Habit>,
    done: Set<String>,
    history: Map<String, Set<String>>,
    todayKey: String,
    onOpen: (Int) -> Unit
) {
    val total = habits.size
    val doneCount = habits.count { it.id in done }
    val percent = if (total == 0) 0 else doneCount * 100 / total

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
                        listOf(Color(0xFFBFD7FF), Color(0xFFD9F3E6), LuBackground)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Halo, $userName! 👋", fontSize = 14.sp, color = LuTextGray)
                        Text("Level Up", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = LuTeal)
                    }
                }
                Spacer(Modifier.height(18.dp))
                ScoreCard(percent, doneCount, total)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickTile("Up Live", LuLiveBg, Modifier.weight(1f), onClick = { onOpen(0) }) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = LuLiveIcon)
                }
                QuickTile("Up Task", LuTaskBg, Modifier.weight(1f), onClick = { onOpen(2) }) {
                    Icon(Icons.Default.Create, contentDescription = null, tint = LuTaskIcon)
                }
                QuickTile("Up Save", LuSaveBg, Modifier.weight(1f), onClick = { onOpen(1) }) {
                    Text("⚡", fontSize = 22.sp)
                }
                QuickTile("Manual Book", LuBookBg, Modifier.weight(1f), onClick = { onOpen(3) }) {
                    Text("📖", fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Ringkasan hari ini", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(12.dp))
            if (habits.isEmpty()) {
                Text("Menyiapkan rutinitasmu...", fontSize = 13.sp, color = LuTextGray)
            }
            HabitModule.entries.forEach { module ->
                val items = habits.filter { it.module == module }
                if (items.isNotEmpty()) {
                    ModuleSummaryCard(module, items, done) { onOpen(module.ordinal) }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Riwayat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(12.dp))
            val days = history.keys.sortedDescending().take(7)
            if (days.isEmpty()) {
                Text(
                    "Riwayat muncul setelah kamu mencentang rutinitas di salah satu fitur.",
                    fontSize = 13.sp,
                    color = LuTextGray
                )
            }
            days.forEach { key ->
                HistoryRow(key, todayKey, habits, history[key] ?: emptySet())
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ScoreCard(percent: Int, done: Int, total: Int) {
    val message = when {
        total == 0 -> "Menyiapkan rutinitasmu..."
        percent == 0 -> "Yuk, mulai dari satu langkah kecil."
        percent < 50 -> "Awal yang bagus, lanjutkan!"
        percent < 100 -> "Hampir selesai, sedikit lagi!"
        else -> "Luar biasa! Semua selesai hari ini 🎉"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreRing(percent)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("Skor kedisiplinan hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuPrimary)
                Spacer(Modifier.height(4.dp))
                Text(message, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuSkySoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⭐ ${done * 10} poin · $done/$total selesai", fontSize = 11.sp, color = LuTextDark)
                }
            }
        }
    }
}

@Composable
private fun ScoreRing(percent: Int) {
    val progress by animateFloatAsState(percent / 100f, tween(600), label = "ring")
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
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
                    color = LuPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Text("$percent%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
    }
}

@Composable
private fun QuickTile(
    label: String,
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark, maxLines = 1)
        }
    }
}

// Kartu ringkasan per fitur di Beranda: hanya progres, tanpa centang. Ketuk untuk membuka fiturnya.
@Composable
private fun ModuleSummaryCard(
    module: HabitModule,
    items: List<Habit>,
    done: Set<String>,
    onClick: () -> Unit
) {
    val fg = moduleColors(module).second
    val doneInModule = items.count { it.id in done }
    val progress = if (items.isEmpty()) 0f else doneInModule.toFloat() / items.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModuleIcon(module, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${moduleName(module)} · ${module.label}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuTextDark
                    )
                    Text("$doneInModule/${items.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = fg,
                    trackColor = LuTrack
                )
            }
        }
    }
}

private fun dayLabel(key: String, todayKey: String): String {
    if (key == todayKey) return "Hari ini"
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    if (key == fmt.format(cal.time)) return "Kemarin"
    val date = runCatching { fmt.parse(key) }.getOrNull() ?: return key
    return SimpleDateFormat("EEE, d MMM", Locale.forLanguageTag("id-ID")).format(date)
}

// Satu baris riwayat: tanggal + hasil centang di tiap fitur pada hari itu.
@Composable
private fun HistoryRow(dayKey: String, todayKey: String, habits: List<Habit>, doneThatDay: Set<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(dayLabel(dayKey, todayKey), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitModule.entries.forEach { module ->
                    val items = habits.filter { it.module == module }
                    val count = items.count { it.id in doneThatDay }
                    val (bg, fg) = moduleColors(module)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(bg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${moduleName(module).removePrefix("Up ")} $count/${items.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fg
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- LAYAR FITUR (tempat centang)

@Composable
private fun ModuleScreen(
    module: HabitModule,
    habits: List<Habit>,
    done: Set<String>,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit
) {
    val items = habits.filter { it.module == module }
    val doneCount = items.count { it.id in done }
    val progress = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size
    val fg = moduleColors(module).second
    val hint = when (module) {
        HabitModule.HEALTH -> "Pengatur jam tidur dan catatan makan akan menyusul di sini."
        HabitModule.SANITATION -> "Daftar perangkat elektronik milikmu sendiri akan menyusul di sini."
        HabitModule.EDUCATION -> "Timer Pomodoro dan daftar tugas akan menyusul di sini."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = LuTextDark)
            }
            Spacer(Modifier.width(4.dp))
            ModuleIcon(module, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(moduleName(module), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Text(module.label, fontSize = 12.sp, color = LuTextGray)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LuSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Progres hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
                Spacer(Modifier.height(4.dp))
                Text(
                    "$doneCount dari ${items.size} selesai",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuTextDark
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = fg,
                    trackColor = LuTrack
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Rutinitas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LuSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (items.isEmpty()) {
                    Text("Menyiapkan rutinitasmu...", fontSize = 13.sp, color = LuTextGray, modifier = Modifier.padding(8.dp))
                }
                items.forEach { h ->
                    val checked = h.id in done
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggle(h.id, !checked) }
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckCircle(checked, fg)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            h.title,
                            fontSize = 15.sp,
                            color = if (checked) LuTextGray else LuTextDark,
                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(hint, fontSize = 12.sp, color = LuTextGray)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ManualBookScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = LuTextDark)
            }
            Text("Manual Book", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tips dan panduan singkat segera hadir.", color = LuTextGray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CheckCircle(checked: Boolean, tint: Color) {
    val fill by animateColorAsState(if (checked) tint else Color.Transparent, label = "check")
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, if (checked) tint else LuBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}
