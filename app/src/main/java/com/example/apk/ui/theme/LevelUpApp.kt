// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/ui/LevelUpApp.kt
// Pengatur layar utama. Beranda = ringkasan + riwayat. Fitur dibuka dari tab Jelajah.
// Perubahan tahap ini: navigasi (Up Save di bawah tab Jelajah), layar Jelajah, gaya dialog Keluar.
package com.example.apk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animatio// LOKASI: app/src/main/java/com/example/apk/ui/LiveScreen.kt (TIMPA seluruh isi)
// Layar Up Live: Ritme Tidur, Hidrasi, Pola Makan.
package com.example.apk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.apk.data.HabitRepository
import com.example.apk.data.LiveRepository
import com.example.apk.data.LiveSettings
import com.example.apk.data.WaterPlan
import com.example.apk.data.WaterRepository
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import java.util.Locale

private val LiveIndigo = Color(0xFF4F46E5)
private val LiveIndigoFrom = Color(0xFFA5B4FC)
private val LiveIndigoSoft = Color(0xFFEEF0FF)
private val LiveAmber = Color(0xFFB45309)
private val LiveAmberBg = Color(0xFFFFF0CC)
private val LiveGreen = Color(0xFF27804D)
private val LiveGreenBg = Color(0xFFD3F0DE)
private val LiveOrange = Color(0xFFEA580C)

private data class LiveMeal(
    val key: String,
    val label: String,
    val window: String,
    val icon: ImageVector,
    val from: Color,
    val to: Color
)

private val liveMeals = listOf(
    LiveMeal("breakfast", "Sarapan", "Pagi · 06.00–09.00", Icons.Default.BreakfastDining, Color(0xFFFDE68A), Color(0xFFD97706)),
    LiveMeal("lunch", "Makan siang", "Siang · 11.30–14.00", Icons.Default.LunchDining, Color(0xFFFDBA74), Color(0xFFEA580C)),
    LiveMeal("dinner", "Makan malam", "Malam · 17.30–20.00", Icons.Default.DinnerDining, Color(0xFFFCA5A5), Color(0xFFDC2626))
)

@Composable
fun LiveScreen(
    done: Set<String>,
    todayKey: String,
    onOpenWater: () -> Unit,
    onBack: () -> Unit
) {
    var settings by remember { mutableStateOf(LiveSettings.DEFAULT) }
    var meals by remember { mutableStateOf<Set<String>>(emptySet()) }
    var waterCount by remember { mutableIntStateOf(0) }
    var waterPlan by remember { mutableStateOf<WaterPlan?>(null) }
    var picker by remember { mutableIntStateOf(0) } // 0 = tertutup, 1 = waktu tidur, 2 = waktu bangun

    val scroll = rememberScrollState()
    val scrolled by remember { derivedStateOf { scroll.value > 40 } }
    val scrimAlpha by animateFloatAsState(if (scrolled) 0.96f else 0f, tween(150), label = "liveScrim")

    DisposableEffect(todayKey) {
        val l1 = LiveRepository.observeSettings { settings = it }
        val l2 = LiveRepository.observeMeals(todayKey) { meals = it }
        val l3 = WaterRepository.observeWater(todayKey) { waterCount = it }
        val l4 = WaterRepository.observePlan { waterPlan = it }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
            l4?.remove()
        }
    }

    fun updateSettings(newSettings: LiveSettings) {
        settings = newSettings
        LiveRepository.saveSettings(newSettings)
    }

    fun toggleSleep() {
        HabitRepository.setDone(todayKey, "sleep", "sleep" !in done)
    }

    // Rutinitas "Makan teratur" di Beranda selesai kalau ketiga waktu makan sudah dicentang.
    fun toggleMeal(key: String) {
        val on = key !in meals
        LiveRepository.setMeal(todayKey, key, on)
        val updated = if (on) meals + key else meals - key
        meals = updated
        val all = liveMeals.all { it.key in updated }
        if (all != ("meal" in done)) HabitRepository.setDone(todayKey, "meal", all)
    }

    val sleepOk = "sleep" in done
    val waterOk = "water" in done
    val mealOk = "meal" in done
    val doneCount = listOf(sleepOk, waterOk, mealOk).count { it }
    val waterTarget = waterPlan?.targetGlasses ?: WaterRepository.DEFAULT_TARGET

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFCFE3FF), Color(0xFFE8F2FF), LuBackground)
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
                            Text(
                                "UP LIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LuLiveIcon
                            )
                            Text("Kesehatan Harian", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LiveHeaderCard(doneCount, sleepOk, waterOk, mealOk)
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                LiveSleepCard(
                    s = settings,
                    sleepOk = sleepOk,
                    onPickBed = { picker = 1 },
                    onPickWake = { picker = 2 },
                    onWindDown = { updateSettings(settings.copy(windDownMinutes = it)) },
                    onToggleSleep = { toggleSleep() }
                )

                Spacer(Modifier.height(14.dp))
                LiveWaterCard(waterCount, waterTarget, waterOk, onOpenWater)

                Spacer(Modifier.height(14.dp))
                LiveMealCard(meals) { toggleMeal(it) }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = LuTextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Pengingat otomatis untuk jeda layar dan jam tidur hadir di tahap berikutnya.",
                        fontSize = 12.sp,
                        color = LuTextGray
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Penutup status bar: muncul saat layar digulir supaya jam dan baterai tetap terbaca.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(LuBackground.copy(alpha = scrimAlpha))
        )
    }

    if (picker == 1) {
        LiveTimeDialog(
            title = "Waktu tidur",
            initial = settings.bedMinutes,
            onDismiss = { picker = 0 },
            onConfirm = {
                updateSettings(settings.copy(bedMinutes = it))
                picker = 0
            }
        )
    }
    if (picker == 2) {
        LiveTimeDialog(
            title = "Waktu bangun",
            initial = settings.wakeMinutes,
            onDismiss = { picker = 0 },
            onConfirm = {
                updateSettings(settings.copy(wakeMinutes = it))
                picker = 0
            }
        )
    }
}

// ---------------------------------------------------------------- KARTU

@Composable
private fun LiveHeaderCard(doneCount: Int, sleepOk: Boolean, waterOk: Boolean, mealOk: Boolean) {
    val progress by animateFloatAsState(doneCount / 3f, tween(500), label = "liveRing")
    val message = when (doneCount) {
        0 -> "Mulai dari satu kebiasaan kecil hari ini."
        1 -> "Bagus, lanjutkan ritmenya."
        2 -> "Tinggal satu lagi!"
        else -> "Tubuhmu terawat hari ini."
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
                            color = LuLiveIcon,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Text("$doneCount/3", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Skor kesehatan hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuLiveIcon)
                Spacer(Modifier.height(2.dp))
                Text(message, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LiveChip("Tidur", sleepOk)
                    LiveChip("Hidrasi", waterOk)
                    LiveChip("Makan", mealOk)
                }
            }
        }
    }
}

@Composable
private fun LiveChip(label: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (ok) LuLiveIcon else LuLiveBg)
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
private fun LiveSleepCard(
    s: LiveSettings,
    sleepOk: Boolean,
    onPickBed: () -> Unit,
    onPickWake: () -> Unit,
    onWindDown: (Int) -> Unit,
    onToggleSleep: () -> Unit
) {
    val duration = liveDuration(s.bedMinutes, s.wakeMinutes)
    val enough = duration >= 420
    val windOptions = listOf(15, 30, 45, 60).let {
        if (s.windDownMinutes in it) it else (it + s.windDownMinutes).sorted()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge(Icons.Default.Bedtime, LiveIndigoFrom, LiveIndigo, 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ritme Tidur", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Text("Jadwal tidur dan bangun yang konsisten", fontSize = 12.sp, color = LuTextGray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (enough) LiveGreenBg else LiveAmberBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        liveDurationText(duration),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enough) LiveGreen else LiveAmber
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            LiveSleepBar(s.bedMinutes, s.wakeMinutes)

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiveTimeTile(Icons.Default.Bedtime, "Tidur", liveFmt(s.bedMinutes), Modifier.weight(1f), onPickBed)
                LiveTimeTile(Icons.Default.WbSunny, "Bangun", liveFmt(s.wakeMinutes), Modifier.weight(1f), onPickWake)
            }

            if (!enough) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dewasa umumnya disarankan tidur sekitar 7 jam atau lebih.",
                    fontSize = 12.sp,
                    color = LiveAmber
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = LiveIndigo,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Jeda layar sebelum tidur",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuTextDark
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                windOptions.forEach { m ->
                    val active = s.windDownMinutes == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) LiveIndigo else LiveIndigoSoft)
                            .clickable { onWindDown(m) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$m mnt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Color.White else LuTextDark
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Mulai berhenti main layar pukul ${liveFmt(s.bedMinutes - s.windDownMinutes)}",
                fontSize = 12.sp,
                color = LuTextGray
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = LuBorder)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleSleep)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveCheck(sleepOk, LiveIndigo)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tidur tepat waktu semalam",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuTextDark
                    )
                    Text(
                        if (sleepOk) "Tercatat. Mantap!" else "Ketuk kalau kamu tidur dan bangun sesuai jadwal",
                        fontSize = 11.sp,
                        color = LuTextGray
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveTimeTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(LiveIndigoSoft)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = LiveIndigo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuTextGray)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Text("Ketuk untuk ubah", fontSize = 11.sp, color = LiveIndigo)
    }
}

@Composable
private fun LiveSleepBar(bed: Int, wake: Int) {
    Column {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val h = size.height
            val w = size.width
            val r = CornerRadius(h / 2, h / 2)
            drawRoundRect(color = LuTrack, topLeft = Offset(0f, 0f), size = size, cornerRadius = r)
            val bx = w * bed / 1440f
            val wx = w * wake / 1440f
            if (wake > bed) {
                drawRoundRect(LiveIndigo, Offset(bx, 0f), Size(wx - bx, h), r)
            } else {
                drawRoundRect(LiveIndigo, Offset(bx, 0f), Size(w - bx, h), r)
                drawRoundRect(LiveIndigo, Offset(0f, 0f), Size(wx, h), r)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("00", "06", "12", "18", "24").forEach {
                Text(it, fontSize = 10.sp, color = LuTextGray)
            }
        }
    }
}

@Composable
private fun LiveWaterCard(count: Int, target: Int, ok: Boolean, onOpen: () -> Unit) {
    val progress = if (target == 0) 0f else (count.toFloat() / target).coerceAtMost(1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge(Icons.Default.WaterDrop, Color(0xFF7DD3FC), Color(0xFF3B82F6), 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Hidrasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Text(
                        if (ok) "Target tercapai · $count dari $target gelas" else "$count dari $target gelas hari ini",
                        fontSize = 12.sp,
                        color = LuTextGray
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuLiveBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = LuLiveIcon, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Catat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuLiveIcon)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = LuLiveIcon,
                trackColor = LuTrack
            )
        }
    }
}

@Composable
private fun LiveMealCard(meals: Set<String>, onToggle: (String) -> Unit) {
    val count = liveMeals.count { it.key in meals }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pola Makan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Text("Tandai saat kamu sudah makan", fontSize = 12.sp, color = LuTextGray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFEDD5))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("$count/3", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LiveOrange)
                }
            }
            Spacer(Modifier.height(6.dp))
            liveMeals.forEach { meal ->
                LiveMealRow(meal, meal.key in meals) { onToggle(meal.key) }
            }
        }
    }
}

@Composable
private fun LiveMealRow(meal: LiveMeal, checked: Boolean, onToggle: () -> Unit) {
    val bg by animateColorAsState(
        if (checked) Color(0xFFFFF3E3) else Color.Transparent,
        label = "mealBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveBadge(meal.icon, meal.from, meal.to, 42.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(meal.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
            Text(
                if (checked) "Sudah makan" else meal.window,
                fontSize = 12.sp,
                color = if (checked) meal.to else LuTextGray
            )
        }
        LiveCheck(checked, meal.to)
    }
}

// ---------------------------------------------------------------- DIALOG PEMILIH JAM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveTimeDialog(
    title: String,
    initial: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial / 60,
        initialMinute = initial % 60,
        is24Hour = true
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = LuSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuTextDark,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                TimePicker(state = state)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("Simpan") }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- KOMPONEN KECIL

@Composable
private fun LiveBadge(icon: ImageVector, from: Color, to: Color, size: Dp) {
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

@Composable
private fun LiveCheck(checked: Boolean, tint: Color) {
    val fill by animateColorAsState(if (checked) tint else Color.Transparent, label = "liveCheck")
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, if (checked) tint else LuBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

// ---------------------------------------------------------------- WAKTU

private fun liveFmt(minutes: Int): String {
    val m = ((minutes % 1440) + 1440) % 1440
    return String.format(Locale.US, "%02d:%02d", m / 60, m % 60)
}

private fun liveDuration(bed: Int, wake: Int): Int =
    if (wake > bed) wake - bed else wake + 1440 - bed

private fun liveDurationText(duration: Int): String {
    val h = duration / 60
    val m = duration % 60
    return if (m == 0) "$h jam" else "$h jam $m mnt"
}n.core.animateFloatAsState
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
fun ProfileScreen(name: String, email: String, onLogout: () -> Unit) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"
    }

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
        Text("Level Up · Versi $version", fontSize = 12.sp, color = LuTextGray)
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = LuSurface,
            titleContentColor = LuTextDark,
            textContentColor = LuTextGray,
            title = { Text("Keluar dari akun?") },
            text = { Text("Kamu perlu masuk lagi untuk memakai Level Up.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onLogout()
                }) {
                    Text("Keluar", color = LogoutRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Batal", color = LuTextGray, fontWeight = FontWeight.SemiBold)
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

// Layar Jelajah: pintu masuk ke tiga fitur (urutan sesuai desain: Up Live, Up Task, Up Save).
@Composable
private fun ExploreScreen(
    habits: List<Habit>,
    done: Set<String>,
    onOpen: (Int) -> Unit
) {
    val order = listOf(HabitModule.HEALTH, HabitModule.EDUCATION, HabitModule.SANITATION)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Jelajah", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(4.dp))
        Text("Mau mulai dari mana?", fontSize = 14.sp, color = LuTextGray)
        Spacer(Modifier.height(20.dp))
        order.forEach { module ->
            val items = habits.filter { it.module == module }
            ExploreCard(module, items, done) { onOpen(module.ordinal) }
            Spacer(Modifier.height(12.dp))
        }
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

// ---------------------------------------------------------------- LAYAR FITUR (Up Live dan Up Task, sementara)

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
                    // "water" tidak dicentang manual: selesai otomatis saat target gelas tercapai.
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
