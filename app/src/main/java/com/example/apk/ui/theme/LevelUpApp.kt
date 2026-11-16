// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/ui/LevelUpApp.kt
// Pengatur layar utama. Beranda = ringkasan + riwayat. Fitur dibuka dari tab Jelajah.
// Perubahan tahap ini: navigasi (Up Save di bawah tab Jelajah), layar Jelajah, gaya dialog Keluar.
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
import kotlinx.coroutines.delay
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

// Indeks layar fitur: 0 = Up Live (Kesehatan), 1 = Up Save (Sanitasi), 2 = Up Task (Pendidikan), 3 = Manual Book,
// 4 = Minum Air Putih (dibuka dari Up Live; tombol Kembali membawa ke Up Live).
// Tab: 0 Beranda, 1 Jelajah, 2 Progres, 3 Profil.
// Up Live / Up Save / Up Task dibuka di bawah tab Jelajah (tab Jelajah menyala). Kalau dibuka dari
// Beranda, tombol Kembali membawamu kembali ke Beranda. Manual Book tetap di bawah Beranda.
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

    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var done by remember { mutableStateOf<Set<String>>(emptySet()) }
    var history by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
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
