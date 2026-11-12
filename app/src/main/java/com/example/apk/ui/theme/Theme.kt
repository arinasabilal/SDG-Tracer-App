package com.example.apk_levelup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------- Palet warna Level Up (diambil dari desain Figma) ----------
val LuBackground = Color(0xFFF4FAF9)   // latar semua layar
val LuSurface = Color(0xFFFFFFFF)      // kartu putih
val LuPrimary = Color(0xFF2563EB)      // tombol utama, tautan
val LuPrimarySoft = Color(0xFFDBEAFE)  // kotak ikon biru muda
val LuSky = Color(0xFF519ED5)          // tombol Simpan / bar progres
val LuSkySoft = Color(0xFFDCEEFF)      // kartu Ide Kebiasaan
val LuTeal = Color(0xFF218C89)         // menu navigasi aktif, lonceng
val LuTextDark = Color(0xFF0F172A)     // teks utama
val LuTextGray = Color(0xFF64748B)     // teks keterangan
val LuBorder = Color(0xFFE2E8F0)       // garis pinggir input
val LuTrack = Color(0xFFD9D9D9)        // jalur progress bar

// Warna per modul
val LuLiveBg = Color(0xFFDBEAFE)
val LuLiveIcon = Color(0xFF2563EB)
val LuTaskBg = Color(0xFFFFF0CC)
val LuTaskIcon = Color(0xFFA87820)
val LuSaveBg = Color(0xFFDDF5E6)
val LuSaveIcon = Color(0xFF27804D)
val LuBookBg = Color(0xFFF0E5FF)
val LuBookIcon = Color(0xFF7950B3)

private val LevelUpColors = lightColorScheme(
    primary = LuPrimary,
    onPrimary = Color.White,
    primaryContainer = LuPrimarySoft,
    onPrimaryContainer = LuPrimary,
    secondary = LuTeal,
    background = LuBackground,
    onBackground = LuTextDark,
    surface = LuSurface,
    onSurface = LuTextDark,
    onSurfaceVariant = LuTextGray,
    outline = LuBorder
)

private val LevelUpTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun LevelUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LevelUpColors,
        typography = LevelUpTypography,
        content = content
    )
}
