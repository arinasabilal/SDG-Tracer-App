package com.example.apk.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

enum class Stage { SPLASH, LOGIN, MAIN }

// Pengatur layar utama: splash -> masuk -> aplikasi
@Composable
fun LevelUpApp() {
    var stage by rememberSaveable { mutableStateOf(Stage.SPLASH) }
    when (stage) {
        Stage.SPLASH -> SplashScreen(onFinished = { stage = Stage.LOGIN })
        Stage.LOGIN -> LoginScreen(onLogin = { stage = Stage.MAIN })
        Stage.MAIN -> MainShell()
    }
}

private data class NavTab(val label: String, val icon: ImageVector)

@Composable
fun MainShell() {
    val tabs = listOf(
        NavTab("Beranda", Icons.Default.Home),
        NavTab("Jelajah", Icons.Default.Search),
        NavTab("Progres", Icons.Default.DateRange),
        NavTab("Profil", Icons.Default.Person)
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = LuBackground,
        bottomBar = {
            NavigationBar(containerColor = LuSurface) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuTeal,
                            selectedTextColor = LuTeal,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = LuTextGray,
                            unselectedTextColor = LuTextGray
                        )
                    )
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
                0 -> HomeScreen()
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
fun HomeScreen() {
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
                Text("Halo, Maya!", fontSize = 13.sp, color = LuTextGray)
                Text("Level Up", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = LuTeal)
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ide Kebiasaan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Text("Lihat semua", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuPrimary)
        }
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LuSkySoft)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Up Live", fontSize = 11.sp, color = LuPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Minum 2 gelas air sebelum sarapan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuTextDark
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("5 menit • pagi", fontSize = 12.sp, color = LuTextGray)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💧", fontSize = 24.sp)
                }
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
        Text("History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(8.dp))
        Text(
            "Riwayat kebiasaanmu akan muncul di sini.",
            fontSize = 13.sp,
            color = LuTextGray
        )
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
