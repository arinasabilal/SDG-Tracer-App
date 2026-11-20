// LOKASI FILE: app/src/main/java/com/example/apk/ui/theme/LevelUpApp.kt
// Pengatur layar utama. Beranda = ringkasan + riwayat. Fitur dibuka dari tab Jelajah.
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.auth.AuthManager
import com.example.apk.data.Habit
import com.example.apk.data.HabitModule
import com.example.apk.data.HabitRepository
import com.example.apk.data.LiveStore
import com.example.apk.data.TaskRepository
import com.example.apk.notif.ReminderPrefs
import com.example.apk.notif.ReminderScheduler
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBookBg
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSaveIcon
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTaskBg
import com.example.apk.ui.theme.LuTaskIcon
import com.example.apk.ui.theme.LuTeal
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val LogoutRed = Color(0xFFD32F2F)

enum class Stage { SPLASH, LOGIN, REGISTER, FORGOT, MAIN }

@Composable
fun LevelUpApp() {
    var stage by rememberSaveable { mutableStateOf(Stage.SPLASH) }
    val appContext = LocalContext.current.applicationContext
    
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
                    ReminderPrefs.clearUserData(appContext)
                    ReminderScheduler.cancelAll(appContext)
                    stage = Stage.LOGIN
                }
            )
        }
    }
}

private data class NavTab(val label: String, val icon: ImageVector)

// Indeks layar fitur: 0 = Up Live (Kesehatan), 1 = Up Save (Sanitasi), 2 = Up Task (Pendidikan), 3 = Manual Book,
// 4 = Minum Air Putih (dibuka dari Up Live; tombol Kembali membawa ke Up Live).
// Tab: 0 Beranda, 1 Jelajah, 2 Progres, 3 Profil.
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
    var cameFromHome by rememberSaveable { mutableStateOf(false) }
    val userName = remember { AuthManager.displayName }
    val userEmail = remember { AuthManager.email }
    
    val shellContext = LocalContext.current.applicationContext
    val requestNotifPermission = rememberNotificationPermissionRequester()
    LaunchedEffect(Unit) {
        if (ReminderPrefs.enabled(shellContext)) requestNotifPermission()
    }
    DisposableEffect(Unit) {
        val r1 = LiveStore.observeSleep { p ->
            ReminderPrefs.setSleep(shellContext, p.bedMinutes, p.wakeMinutes, p.windDownMinutes)
            ReminderScheduler.rescheduleAll(shellContext)
        }
        val r2 = TaskRepository.observeTasks { list ->
            ReminderPrefs.setTasks(
                shellContext,
                list.filter { !it.done && it.deadline.isNotEmpty() }.map { it.deadline to it.title }
            )
            ReminderScheduler.rescheduleAll(shellContext)
        }
        onDispose {
            r1?.remove()
            r2?.remove()
        }
    }

    // Tanggal hari ini dicek ulang tiap 30 detik. Setelah lewat tengah malam, key berganti,
    // pendengar data dipasang ulang untuk tanggal baru, dan centang hari baru mulai kosong.
    var todayKey by remember { mutableStateOf(HabitRepository.todayKey()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            val now = HabitRepository.todayKey()
            if (now != todayKey) todayKey = now
        }
    }

    val initialHabits: List<Habit> = emptyList()
    val initialDone: Set<String> = emptySet()
    val initialHistory: Map<String, Set<String>> = emptyMap()

    var habits by remember { mutableStateOf(initialHabits) }
    var done by remember { mutableStateOf(initialDone) }
    var history by remember { mutableStateOf(initialHistory) }
    var seeded by remember { mutableStateOf(false) }

    DisposableEffect(todayKey) {
        val key = todayKey
        if (!seeded) {
            HabitRepository.seedDefaultsIfEmpty()
            seeded = true
        }
        done = emptySet()
        val l1 = HabitRepository.observeHabits { habits = it }
        val l2 = HabitRepository.observeDone(key) { done = it }
        val l3 = HabitRepository.observeHistory(PROGRESS_LOOKBACK_DAYS.toLong()) { history = it }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
        }
    }

    fun openModule(index: Int, fromHome: Boolean) {
        if (index == 3) {
            openIdx = 3
        } else {
            selected = 1
            openIdx = index
            cameFromHome = fromHome
        }
    }

    fun closeModule() {
        if (openIdx == 4) {
            // Dari Minum Air Putih kembali ke layar Up Live.
            openIdx = 0
            return
        }
        openIdx = -1
        if (cameFromHome) {
            selected = 0
            cameFromHome = false
        }
    }

    BackHandler(enabled = openIdx >= 0) { closeModule() }

    Scaffold(
        containerColor = LuBackground,
        bottomBar = {
            BottomBar(tabs, selected) {
                selected = it
                openIdx = -1
                cameFromHome = false
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(bottom = inner.calculateBottomPadding())
                .fillMaxSize()
        ) {
            when {
                selected == 0 && openIdx == 3 -> ManualBookScreen(onBack = { closeModule() })
                selected == 1 && openIdx == 1 -> SaveScreen(
                    done = done,
                    todayKey = todayKey,
                    onBack = { closeModule() }
                )
                selected == 1 && openIdx == 4 -> WaterScreen(
                    todayKey = todayKey,
                    onBack = { closeModule() }
                )
                selected == 1 && openIdx == 0 -> LiveScreen(
                    done = done,
                    todayKey = todayKey,
                    onOpenWater = { openIdx = 4 },
                    onBack = { closeModule() }
                )
                selected == 1 && openIdx == 2 -> TaskScreen(
                    done = done,
                    todayKey = todayKey,
                    onBack = { closeModule() }
                )
                selected == 1 && openIdx in 0..2 -> ModuleScreen(
                    module = HabitModule.entries[openIdx],
                    habits = habits,
                    done = done,
                    onBack = { closeModule() },
                    onToggle = { id, value -> HabitRepository.setDone(todayKey, id, value) },
                    onOpenWater = { openIdx = 4 }
                )
                selected == 1 -> ExploreScreen(
                    habits = habits,
                    done = done,
                    onOpen = { openModule(it, fromHome = false) }
                )
                selected == 0 -> HomeScreen(
                    userName = userName,
                    habits = habits,
                    done = done,
                    history = history,
                    todayKey = todayKey,
                    onOpen = { openModule(it, fromHome = true) }
                )
                selected == 2 -> ProgressScreen(
                    habits = habits,
                    history = history,
                    done = done,
                    todayKey = todayKey
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
fun ProfileScreen(
    name: String,
    email: String,
    onLogout: () -> Unit
) {
    val displayName = name.takeIf { it.isNotBlank() } ?: "Pengguna"
    val displayEmail = email.takeIf { it.isNotBlank() } ?: "-"
    val initial = displayName.first().uppercaseChar().toString()

    // State Informasi Pribadi
    var gender by rememberSaveable { mutableStateOf("Perempuan") }
    var birthPlace by rememberSaveable { mutableStateOf("Gresik") }
    var birthDate by rememberSaveable { mutableStateOf("2007-03-24") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }

    // State Pengaturan Aplikasi
    val reminderContext = LocalContext.current.applicationContext
    var smartReminder by remember { mutableStateOf(ReminderPrefs.enabled(reminderContext)) }
    val requestNotifPermission = rememberNotificationPermissionRequester()
    var darkMode by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Profil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = LuTextDark,
            modifier = Modifier.padding(top = 12.dp, bottom = 14.dp)
        )

        // 1. KARTU IDENTITAS UTAMA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE8F1FE), Color(0xFFE3F5EE))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(LuPrimary, Color(0xFF0F766E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Spacer(Modifier.height(2.dp))
                        Text(displayEmail, fontSize = 13.sp, color = LuTextGray)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LuPrimary.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Mahasiswa • Target Disiplin Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LuPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. RINGKASAN STATISTIK KEDISIPLINAN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("7 Hari", "Daily Streak", Color(0xFFD97706)),
                Triple("88%", "Skor Disiplin", Color(0xFF0F766E)),
                Triple("12 Sesi", "Fokus Belajar", LuPrimary)
            ).forEach { (value, label, accent) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = LuSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accent)
                        Spacer(Modifier.height(2.dp))
                        Text(label, fontSize = 11.sp, color = LuTextGray, maxLines = 1)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. KARTU INFORMASI PRIBADI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LuSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INFORMASI PRIBADI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuPrimary,
                        letterSpacing = 0.6.sp
                    )
                    TextButton(
                        onClick = { showEditDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Ubah",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuPrimary
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Jenis Kelamin", fontSize = 13.sp, color = LuTextGray)
                        Text(gender, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tempat Lahir", fontSize = 13.sp, color = LuTextGray)
                        Text(birthPlace, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tanggal Lahir", fontSize = 13.sp, color = LuTextGray)
                        Text(birthDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4. KARTU PENGATURAN APLIKASI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LuSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Pengaturan Aplikasi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuTextDark
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Smart Departure & Night Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuTextDark
                        )
                        Text(
                            text = "Pengingat lokal offline untuk cek listrik & tidur",
                            fontSize = 12.sp,
                            color = LuTextGray
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = smartReminder,
                        onCheckedChange = {
                            smartReminder = it
                            ReminderPrefs.setEnabled(reminderContext, it)
                            ReminderScheduler.rescheduleAll(reminderContext)
                            if (it) requestNotifPermission()
                        }
                    )
                }
                
                ReminderExtras(enabled = smartReminder)
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Mode Gelap (Dark Mode)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuTextDark
                        )
                        Text(
                            text = "Tampilan nyaman untuk belajar malam hari",
                            fontSize = 12.sp,
                            color = LuTextGray
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 5. TOMBOL KELUAR AKUN
        OutlinedButton(
            onClick = { showLogoutConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, LogoutRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LogoutRed)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Keluar dari akun", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }

    // DIALOG KONFIRMASI KELUAR AKUN
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = LuSurface,
            titleContentColor = LuTextDark,
            textContentColor = LuTextGray,
            title = { Text("Keluar dari akun?") },
            text = { Text("Kamu perlu masuk lagi untuk memakai Level Up.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text("Keluar", color = LogoutRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal", color = LuTextGray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // DIALOG UBAH INFORMASI PRIBADI
    if (showEditDialog) {
        var tempGender by remember { mutableStateOf(gender) }
        var tempBirthPlace by remember { mutableStateOf(birthPlace) }
        var tempBirthDate by remember { mutableStateOf(birthDate) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Ubah Informasi Pribadi", fontWeight = FontWeight.Bold, color = LuTextDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempGender,
                        onValueChange = { tempGender = it },
                        label = { Text("Jenis Kelamin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBirthPlace,
                        onValueChange = { tempBirthPlace = it },
                        label = { Text("Tempat Lahir") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBirthDate,
                        onValueChange = { tempBirthDate = it },
                        label = { Text("Tanggal Lahir (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        gender = tempGender.ifBlank { gender }
                        birthPlace = tempBirthPlace.ifBlank { birthPlace }
                        birthDate = tempBirthDate.ifBlank { birthDate }
                        showEditDialog = false
                    }
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold, color = LuPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", color = LuTextGray)
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

private fun moduleBlurb(m: HabitModule) = when (m) {
    HabitModule.HEALTH -> "Minum air putih, jam tidur, aktivitas fisik"
    HabitModule.SANITATION -> "Matikan perangkat sebelum berangkat dan tidur"
    HabitModule.EDUCATION -> "Sesi belajar, mode baca, mode upgrade"
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

// ---------------------------------------------------------------- JELAJAH

@Composable
private fun ExploreScreen(
    habits: List<Habit>,
    done: Set<String>,
    onOpen: (Int) -> Unit
) {
    val order = listOf(HabitModule.HEALTH, HabitModule.EDUCATION, HabitModule.SANITATION)
    val initialTip: Triple<String, String, String>? = null

    var selectedCategory by rememberSaveable { mutableStateOf("Semua") }
    var selectedTip by remember { mutableStateOf(initialTip) }
    var factIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = listOf("Semua", "SDG 3 · Sehat", "SDG 4 · Belajar", "SDG 6/7 · Hemat")

    val sdgIdeas = listOf(
        Triple(
            "Aturan Mata 20-20-20",
            "SDG 3 · Sehat",
            "Setiap 20 menit menatap layar laptop saat kuliah atau nugas, alihkan pandangan ke objek sejauh 6 meter selama 20 detik agar mata tidak cepat lelah."
        ),
        Triple(
            "Teknik Fokus Pomodoro 25/5",
            "SDG 4 · Belajar",
            "Belajar fokus penuh selama 25 menit tanpa mengecek notifikasi ponsel, lalu istirahat 5 menit. Ulangi 3–4 sesi untuk menjaga konsentrasi tetap tajam."
        ),
        Triple(
            "Putus Vampire Power Elektronik",
            "SDG 6/7 · Hemat",
            "Charger HP dan stopkontak yang tetap menyala saat tidak dipakai tetap menyedot listrik standby. Biasakan mencabutnya sebelum tidur atau keluar kamar."
        ),
        Triple(
            "Target Tumbler 2 Liter Harian",
            "SDG 3 · Sehat",
            "Bawa botol minum isi ulang ke kampus setiap hari. Tubuh terhidrasi optimal sekaligus mengurangi sampah botol plastik sekali pakai."
        ),
        Triple(
            "Review Catatan 10 Menit Malam",
            "SDG 4 · Belajar",
            "Baca ulang ringkasan materi kuliah selama 10 menit sebelum tidur untuk memperkuat memori jangka panjang tanpa perlu sistem kebut semalam."
        )
    )

    val funFacts = listOf(
        "Tahukah kamu? Kurang tidur 2 jam saja dapat menurunkan fokus belajar dan daya ingat jangka pendek hingga 40%.",
        "Tahukah kamu? Mematikan lampu dan mencabut perangkat elektronik sebelum tidur dapat memangkas hingga 10% pemborosan listrik kamar.",
        "Tahukah kamu? Minum segelas air putih segera setelah bangun tidur membantu mempercepat metabolisme dan kesiapan fokus pagi hari."
    )

    val filteredIdeas = if (selectedCategory == "Semua") {
        sdgIdeas
    } else {
        sdgIdeas.filter { it.second == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Jelajah", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(4.dp))
        Text("Temukan modul rutin & inspirasi aksi SDG harian", fontSize = 14.sp, color = LuTextGray)
        Spacer(Modifier.height(16.dp))

        // 1. BANNER WAWASAN SDG INTERAKTIF
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1D4ED8), Color(0xFF0F766E))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "WAWASAN SDG HARI INI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.6.sp
                            )
                        }
                        Text(
                            text = "Ganti Fakta ↻",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA7F3D0),
                            modifier = Modifier.clickable {
                                factIndex = (factIndex + 1) % funFacts.size
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = funFacts[factIndex],
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // 2. EKSPLORASI MODUL UTAMA (3 UP)
        Text(
            text = "Pilih Modul Aktivitas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = LuTextDark
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Masuk langsung ke ruang fokus pilihanmu",
            fontSize = 12.sp,
            color = LuTextGray
        )
        Spacer(Modifier.height(12.dp))

        order.forEach { module ->
            val items = habits.filter { it.module == module }
            ExploreCard(module, items, done) { onOpen(module.ordinal) }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(10.dp))

        // 3. KATALOG IDE KEBIASAAN SDG DENGAN FILTER KATEGORI
        Text(
            text = "Katalog Kebiasaan Mikro",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = LuTextDark
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ketuk kartu untuk melihat panduan & dampaknya",
            fontSize = 12.sp,
            color = LuTextGray
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) LuPrimary else LuSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) LuPrimary else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else LuTextDark
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        filteredIdeas.forEach { item ->
            val badgeColor = when (item.second) {
                "SDG 3 · Sehat" -> LuPrimary
                "SDG 4 · Belajar" -> Color(0xFFD97706)
                else -> Color(0xFF0F766E)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTip = item },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LuSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = item.second,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = item.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuTextDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.third,
                            fontSize = 12.sp,
                            color = LuTextGray,
                            maxLines = 2
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Panduan ›",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuPrimary
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(20.dp))
    }

    selectedTip?.let { tip ->
        AlertDialog(
            onDismissRequest = { selectedTip = null },
            title = {
                Column {
                    Text(tip.second, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text(tip.first, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                }
            },
            text = {
                Text(tip.third, fontSize = 14.sp, color = LuTextDark, lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = { selectedTip = null }) {
                    Text("Siap Terapkan", fontWeight = FontWeight.Bold, color = LuPrimary)
                }
            }
        )
    }
}

@Composable
private fun ExploreCard(
    module: HabitModule,
    items: List<Habit>,
    done: Set<String>,
    onClick: () -> Unit
) {
    val fg = moduleColors(module).second
    val doneCount = items.count { it.id in done }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModuleIcon(module, 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${moduleName(module)} · ${module.label}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuTextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(moduleBlurb(module), fontSize = 12.sp, color = LuTextGray)
                if (items.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$doneCount/${items.size} selesai hari ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = fg
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LuTextGray
            )
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

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ScoreCard(percent: Int, done: Int, total: Int) {
    val message = when {
        total == 0 -> "Menyiapkan rutinitasmu..."
        percent == 0 -> "Yuk, mulai dari satu langkah kecil."
        percent < 50 -> "Awal yang bagus, teruskan ritmenya!"
        percent < 100 -> "Hampir tuntas, tinggal sedikit lagi!"
        else -> "Luar biasa! Semua misi selesai 🎉"
    }
    val statusBadge = when {
        total == 0 -> "⏳ Memuat"
        percent == 0 -> "🌱 Langkah Awal"
        percent < 50 -> "⚡ Terjaga"
        percent < 100 -> "🔥 Hampir Tuntas"
        else -> "🏆 Disiplin Penuh"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E56C5),
                            Color(0xFF0F766E)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreRing(percent)
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Skor kedisiplinan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBAE6FD)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "⭐ ${done * 10} poin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFEF08A)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🎯 $done/$total misi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
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
            val stroke = 11.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = (size.minDimension - stroke) / 2f
            )
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF34D399),
                            Color(0xFFFEF08A),
                            Color(0xFF38BDF8)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "HARI INI",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFBAE6FD),
                letterSpacing = 0.8.sp
            )
        }
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

// ---------------------------------------------------------------- LAYAR FITUR

@Composable
private fun ModuleScreen(
    module: HabitModule,
    habits: List<Habit>,
    done: Set<String>,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onOpenWater: () -> Unit = {}
) {
    val items = habits.filter { it.module == module }
    val doneCount = items.count { it.id in done }
    val progress = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size
    val fg = moduleColors(module).second
    val hint = when (module) {
        HabitModule.HEALTH -> "Minum air dicatat di layar Minum Air Putih. Jam tidur dan aktivitas fisik menyusul."
        HabitModule.SANITATION -> "Daftar perangkat elektronik milikmu sendiri ada di layar Up Save."
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
                    val isWater = h.id == "water"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { if (isWater) onOpenWater() else onToggle(h.id, !checked) }
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckCircle(checked, fg)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                h.title,
                                fontSize = 15.sp,
                                color = if (checked) LuTextGray else LuTextDark,
                                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                            )
                            if (isWater) {
                                Text("Ketuk untuk mencatat gelas", fontSize = 11.sp, color = LuTextGray)
                            }
                        }
                        if (isWater) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = LuTextGray
                            )
                        }
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
