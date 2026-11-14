// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/ui/theme/LevelUpApp.kt
// Pengatur layar utama: splash -> (cek sesi) -> masuk / daftar / lupa sandi -> aplikasi.
// MainActivity.kt TIDAK perlu diubah: LevelUpApp() tetap dipanggil tanpa parameter.
package com.example.apk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.DisposableEffect
import com.example.apk.data.Habit
import com.example.apk.data.HabitModule
import com.example.apk.data.HabitRepository
import com.example.apk.ui.theme.LuSky
import com.example.apk.ui.theme.LuTrack
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.auth.AuthManager
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBookBg
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSkySoft
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTaskBg
import com.example.apk.ui.theme.LuTaskIcon
import com.example.apk.ui.theme.LuTeal
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray

private val LogoutRed = Color(0xFFD32F2F)

enum class Stage { SPLASH, LOGIN, REGISTER, FORGOT, MAIN }

@Composable
fun LevelUpApp() {
    var stage by rememberSaveable { mutableStateOf(Stage.SPLASH) }

    // Tombol Kembali di HP: dari Daftar / Lupa sandi balik ke Masuk (bukan keluar dari app).
    BackHandler(enabled = stage == Stage.REGISTER || stage == Stage.FORGOT) {
        stage = Stage.LOGIN
    }

    Crossfade(targetState = stage, animationSpec = tween(300), label = "stage") { current ->
        when (current) {
            Stage.SPLASH -> SplashScreen(
                // Cek sesi: kalau masih login, lewati layar masuk.
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

@Composable
fun MainShell(onLogout: () -> Unit = {}) {
    val tabs = listOf(
        NavTab("Beranda", Icons.Default.Home),
        NavTab("Jelajah", Icons.Default.Search),
        NavTab("Progres", Icons.Default.DateRange),
        NavTab("Profil", Icons.Default.Person)
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val userName = remember { AuthManager.displayName }
    val userEmail = remember { AuthManager.email }

    Scaffold(
        containerColor = LuBackground,
                bottomBar = {
            Surface(color = LuSurface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val active = selected == index
                        val tint = if (active) LuTeal else LuTextGray
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selected = index },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
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
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            when (selected) {
                0 -> HomeScreen(userName)
                3 -> ProfileScreen(userName, userEmail, onLogout)
                else -> PlaceholderScreen(tabs[selected].label)
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

@Composable
fun HomeScreen(userName: String = "Pengguna") {
    val dateKey = remember { HabitRepository.todayKey() }
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var done by remember { mutableStateOf<Set<String>>(emptySet()) }

    DisposableEffect(Unit) {
        HabitRepository.seedDefaultsIfEmpty()
        val l1 = HabitRepository.observeHabits { habits = it }
        val l2 = HabitRepository.observeDone(dateKey) { done = it }
        onDispose {
            l1?.remove()
            l2?.remove()
        }
    }

    val total = habits.size
    val doneCount = habits.count { it.id in done }
    val percent = if (total == 0) 0 else doneCount * 100 / total

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Halo, $userName!", fontSize = 13.sp, color = LuTextGray)
                Text("Level Up", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = LuTeal)
        }

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LuSkySoft)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Skor kedisiplinan hari ini", fontSize = 12.sp, color = LuPrimary)
                Spacer(Modifier.height(6.dp))
                Text("$percent%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = LuSky,
                    trackColor = LuTrack
                )
                Spacer(Modifier.height(6.dp))
                Text("$doneCount dari $total kebiasaan selesai", fontSize = 12.sp, color = LuTextGray)
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryTile("Up Live", LuLiveBg) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = LuLiveIcon)
            }
            CategoryTile("Up Task", LuTaskBg) {
                Icon(Icons.Default.Create, contentDescription = null, tint = LuTaskIcon)
            }
            CategoryTile("Up Save", LuSaveBg) {
                Text("⚡", fontSize = 22.sp)
            }
            CategoryTile("Manual Book", LuBookBg) {
                Text("📖", fontSize = 22.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Rutinitas hari ini", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(8.dp))

        if (habits.isEmpty()) {
            Text("Menyiapkan rutinitasmu...", fontSize = 13.sp, color = LuTextGray)
        }
        HabitModule.entries.forEach { module ->
            val items = habits.filter { it.module == module }
            if (items.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(module.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTeal)
                items.forEach { h ->
                    val checked = h.id in done
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { HabitRepository.setDone(dateKey, h.id, !checked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { HabitRepository.setDone(dateKey, h.id, it) }
                        )
                        Text(h.title, fontSize = 15.sp, color = LuTextDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(label: String, bg: Color, icon: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
    }
}
