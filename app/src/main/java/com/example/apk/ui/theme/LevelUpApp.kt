// LOKASI: app/src/main/java/com/example/apk/ui/LiveScreen.kt (TIMPA seluruh isi)
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

@Composable
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
}
