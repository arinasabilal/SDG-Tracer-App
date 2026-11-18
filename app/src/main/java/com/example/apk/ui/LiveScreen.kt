// LOKASI: app/src/main/java/com/example/apk/ui/LiveScreen.kt (TIMPA seluruh isi)
// Layar Up Live: Kesehatan Harian (Ritme Tidur, Hidrasi, Pola Makan).
package com.example.apk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.apk.data.HabitRepository
import com.example.apk.data.LiveStore
import com.example.apk.data.SleepPlan
import com.example.apk.data.SleepTracker
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val LiveIndigo = Color(0xFF4F46E5)
private val LiveIndigoFrom = Color(0xFFA5B4FC)
private val LiveIndigoSoft = Color(0xFFEEF0FF)
private val LiveWake = Color(0xFFD97706)
private val LiveWakeSoft = Color(0xFFFFF4DC)
private val LiveDialTrack = Color(0xFFE6EAF6)
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
    LiveMeal("breakfast", "Sarapan", "06.00 – 09.00", Icons.Default.BreakfastDining, Color(0xFFFDE68A), Color(0xFFD97706)),
    LiveMeal("lunch", "Makan siang", "11.30 – 14.00", Icons.Default.LunchDining, Color(0xFFFDBA74), Color(0xFFEA580C)),
    LiveMeal("dinner", "Makan malam", "17.30 – 20.00", Icons.Default.DinnerDining, Color(0xFFFCA5A5), Color(0xFFDC2626))
)

@Composable
fun LiveScreen(
    done: Set<String>,
    todayKey: String,
    onOpenWater: () -> Unit,
    onBack: () -> Unit
) {
    var plan by remember { mutableStateOf(SleepPlan.DEFAULT) }
    var edits by remember { mutableIntStateOf(0) }
    var meals by remember { mutableStateOf<Set<String>>(emptySet()) }
    var bedOk by remember { mutableStateOf(false) }
    var wakeOk by remember { mutableStateOf(false) }
    var waterCount by remember { mutableIntStateOf(0) }
    var waterPlan by remember { mutableStateOf<WaterPlan?>(null) }
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 = sebelum tidur, 1 = bangun tidur
    var tracker by remember { mutableStateOf<SleepTracker?>(null) }

    val doneNow by rememberUpdatedState(done)

    val scroll = rememberScrollState()
    val scrolled by remember { derivedStateOf { scroll.value > 40 } }
    val scrimAlpha by animateFloatAsState(if (scrolled) 0.96f else 0f, tween(150), label = "liveScrim")

    DisposableEffect(todayKey) {
        // Data dari server dipakai selama kamu belum mengubah jadwal di layar ini.
        val l1 = LiveStore.observeSleep { if (edits == 0) plan = it }
        val l2 = LiveStore.observeDay(todayKey) { m, b, w ->
            meals = m
            bedOk = b
            wakeOk = w
        }
        val l3 = WaterRepository.observeWater(todayKey) { waterCount = it }
        val l4 = WaterRepository.observePlan { waterPlan = it }
        val l5 = LiveStore.observeTracker { tracker = it }
        onDispose {
            l1?.remove()
            l2?.remove()
            l3?.remove()
            l4?.remove()
            l5?.remove()
            // Simpan perubahan yang belum sempat tersimpan saat layar ditutup.
            if (edits > 0) LiveStore.saveSleep(plan)
        }
    }

    // Perubahan jadwal disimpan 0,6 detik setelah sentuhan terakhir (supaya tombol tahan tidak menulis berkali-kali).
    LaunchedEffect(edits) {
        if (edits > 0) {
            delay(600)
            LiveStore.saveSleep(plan)
        }
    }

    fun edit(p: SleepPlan) {
        plan = p
        edits++
    }

    fun moveHandle(handle: Int, minutes: Int) {
        edit(if (handle == 0) plan.copy(bedMinutes = minutes) else plan.copy(wakeMinutes = minutes))
    }

    // Memilih periode tracker tidur. Mengetuk pilihan yang sedang berjalan tidak mengubah apa pun,
    // kecuali periodenya sudah selesai: saat itu mengetuknya memulai periode baru dari hari ini.
    fun pickPeriod(days: Int) {
        val t = tracker
        val finished = t != null && t.durationDays > 0 &&
            liveDaysBetween(t.startDate, todayKey) + 1 > t.durationDays
        if (t != null && t.durationDays == days && !finished) return
        tracker = SleepTracker(days, todayKey)
        LiveStore.saveTracker(days, todayKey)
    }

    fun stepWind(delta: Int) {
        edit(plan.copy(windDownMinutes = (plan.windDownMinutes + delta).coerceIn(0, 180)))
    }

    // Rutinitas "Tidur" di Beranda selesai kalau centang sebelum tidur DAN bangun tepat waktu sudah ditandai.
    fun syncSleep(bed: Boolean, wake: Boolean) {
        val both = bed && wake
        if (both != ("sleep" in doneNow)) HabitRepository.setDone(todayKey, "sleep", both)
    }

    fun toggleBed() {
        val on = !bedOk
        bedOk = on
        LiveStore.setSleepFlag(todayKey, "sleepBed", on)
        syncSleep(on, wakeOk)
    }

    fun toggleWake() {
        val on = !wakeOk
        wakeOk = on
        LiveStore.setSleepFlag(todayKey, "sleepWake", on)
        syncSleep(bedOk, on)
    }

    // Rutinitas "Makan teratur" selesai kalau ketiga waktu makan sudah ditandai.
    fun toggleMeal(key: String) {
        val on = key !in meals
        LiveStore.setMeal(todayKey, key, on)
        val updated = if (on) meals + key else meals - key
        meals = updated
        val all = liveMeals.all { it.key in updated }
        if (all != ("meal" in doneNow)) HabitRepository.setDone(todayKey, "meal", all)
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
                        LiveBackButton(onBack)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "UP LIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = LuLiveIcon
                            )
                            Text("Kesehatan Harian", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                            Text("Pantau tidur, hidrasi, dan pola makan", fontSize = 12.sp, color = LuTextGray)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LiveHeaderCard(doneCount, sleepOk, waterOk, mealOk)
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                LiveSleepCard(
                    plan = plan,
                    mode = mode,
                    bedOk = bedOk,
                    wakeOk = wakeOk,
                    onMode = { mode = it },
                    onMove = { h, m -> moveHandle(h, m) },
                    onWindDelta = { stepWind(it) },
                    onWindSet = { edit(plan.copy(windDownMinutes = it)) },
                    tracker = tracker,
                    todayKey = todayKey,
                    onPickPeriod = { pickPeriod(it) },
                    onToggleBed = { toggleBed() },
                    onToggleWake = { toggleWake() }
                )

                Spacer(Modifier.height(14.dp))
                LiveWaterCard(waterCount, waterTarget, onOpenWater)

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
}

// ---------------------------------------------------------------- HEADER

@Composable
private fun LiveBackButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, LuBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Kembali",
            tint = LuTextDark,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun LiveHeaderCard(doneCount: Int, sleepOk: Boolean, waterOk: Boolean, mealOk: Boolean) {
    val progress by animateFloatAsState(doneCount / 3f, tween(500), label = "liveRing")
    val message = when (doneCount) {
        0 -> "Belum ada target tercapai"
        1 -> "1 dari 3 target tercapai"
        2 -> "2 dari 3 target tercapai"
        else -> "Semua target tercapai"
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
                Text("Ringkasan hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuLiveIcon)
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

// ---------------------------------------------------------------- RITME TIDUR

@Composable
private fun LiveSleepCard(
    plan: SleepPlan,
    mode: Int,
    bedOk: Boolean,
    wakeOk: Boolean,
    onMode: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onWindDelta: (Int) -> Unit,
    onWindSet: (Int) -> Unit,
    tracker: SleepTracker?,
    todayKey: String,
    onPickPeriod: (Int) -> Unit,
    onToggleBed: () -> Unit,
    onToggleWake: () -> Unit
) {
    val duration = liveDuration(plan.bedMinutes, plan.wakeMinutes)
    val enough = duration >= 420
    val accent = if (mode == 0) LiveIndigo else LiveWake
    val soft = if (mode == 0) LiveIndigoSoft else LiveWakeSoft
    val activeMinutes = if (mode == 0) plan.bedMinutes else plan.wakeMinutes

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
                    Text("Atur jadwal tidur dan bangun", fontSize = 12.sp, color = LuTextGray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (enough) LiveGreenBg else LiveAmberBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        liveDurationShort(duration),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enough) LiveGreen else LiveAmber
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            LiveSleepSwitch(mode, bedOk, wakeOk, onMode)

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SleepDial(
                    bed = plan.bedMinutes,
                    wake = plan.wakeMinutes,
                    active = mode,
                    onActive = onMode,
                    onMove = onMove
                ) {
                    Text(
                        if (mode == 0) "Waktu tidur" else "Waktu bangun",
                        fontSize = 12.sp,
                        color = LuTextGray
                    )
                    Text(liveFmt(activeMinutes), fontSize = 38.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Spacer(Modifier.height(2.dp))
                    Text("Durasi tidur", fontSize = 11.sp, color = LuTextGray)
                    Text(liveDurationLong(duration), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveStat("Tidur", liveFmt(plan.bedMinutes), mode == 0, LiveIndigo, Modifier.weight(1f)) { onMode(0) }
                LiveStat("Bangun", liveFmt(plan.wakeMinutes), mode == 1, LiveWake, Modifier.weight(1f)) { onMode(1) }
                LiveStat("Durasi", liveDurationShort(duration), false, LiveGreen, Modifier.weight(1f), null)
            }
            if (!enough) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dewasa umumnya disarankan tidur sekitar 7 jam atau lebih.",
                    fontSize = 12.sp,
                    color = LiveAmber
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Geser lingkaran untuk mengatur jam tidur dan bangun (kelipatan 5 menit).",
                fontSize = 11.sp,
                color = LuTextGray
            )

            Spacer(Modifier.height(16.dp))
            LiveTrackerBlock(tracker, todayKey, onPickPeriod)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = LuBorder)
            Spacer(Modifier.height(14.dp))

            if (mode == 0) {
                LiveWindDownBlock(plan, onWindDelta, onWindSet)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = LuBorder)
                Spacer(Modifier.height(4.dp))
                LiveCheckRow(
                    checked = bedOk,
                    tint = LiveIndigo,
                    title = "Rutinitas sebelum tidur",
                    subtitle = if (bedOk) "Tercatat untuk hari ini" else "Tandai setelah menjalankan jeda layar dan tidur tepat waktu",
                    onToggle = onToggleBed
                )
            } else {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = LiveWake, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Target bangun pukul ${liveFmt(plan.wakeMinutes)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuTextDark
                        )
                        Text(
                            "Bangun di jam yang sama setiap hari membantu menjaga ritme tubuh.",
                            fontSize = 12.sp,
                            color = LuTextGray
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = LuBorder)
                Spacer(Modifier.height(4.dp))
                LiveCheckRow(
                    checked = wakeOk,
                    tint = LiveWake,
                    title = "Bangun tepat waktu",
                    subtitle = if (wakeOk) "Tercatat untuk hari ini" else "Tandai jika pagi ini kamu bangun sesuai jadwal",
                    onToggle = onToggleWake
                )
            }
        }
    }
}

// Periode tracker tidur: 1 hari, 1 minggu, 1 bulan, atau Otomatis (berjalan terus tanpa batas waktu).
@Composable
private fun LiveTrackerBlock(tracker: SleepTracker?, todayKey: String, onPick: (Int) -> Unit) {
    val options = listOf(1 to "1 hari", 7 to "1 minggu", 30 to "1 bulan", 0 to "Otomatis")
    val dayNo = if (tracker == null) 0 else liveDaysBetween(tracker.startDate, todayKey) + 1
    val finished = tracker != null && tracker.durationDays > 0 && dayNo > tracker.durationDays
    val info = if (tracker == null) {
        "Pilih berapa lama tracker tidur berjalan."
    } else if (tracker.durationDays == 0) {
        "Berjalan otomatis tanpa batas waktu · mulai ${livePretty(tracker.startDate)}"
    } else if (finished) {
        "Periode selesai. Ketuk pilihan yang sama untuk memulai lagi."
    } else {
        "Hari ke-$dayNo dari ${tracker.durationDays} · berlaku sampai " +
            livePretty(liveAddDays(tracker.startDate, tracker.durationDays - 1))
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = LiveIndigo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Periode tracker", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (days, label) ->
                val active = tracker != null && tracker.durationDays == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) LiveIndigo else LiveIndigoSoft)
                        .clickable { onPick(days) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) Color.White else LuTextDark,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(info, fontSize = 12.sp, color = LuTextGray)
    }
}

@Composable
private fun LiveSleepSwitch(mode: Int, bedOk: Boolean, wakeOk: Boolean, onMode: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF1F4FA))
            .padding(4.dp)
    ) {
        listOf(
            Triple("Sebelum Tidur", Icons.Default.Bedtime, bedOk),
            Triple("Bangun Tidur", Icons.Default.WbSunny, wakeOk)
        ).forEachIndexed { i, (label, icon, ok) ->
            val active = mode == i
            val activeColor = if (i == 0) LiveIndigo else LiveWake
            val bg by animateColorAsState(if (active) activeColor else Color.Transparent, label = "sleepMode")
            val fg = if (active) Color.White else LuTextGray
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onMode(i) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
                if (ok) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Check, contentDescription = "Tercatat", tint = fg, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun LiveStat(
    label: String,
    value: String,
    selected: Boolean,
    tint: Color,
    modifier: Modifier,
    onClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) tint.copy(alpha = 0.12f) else Color(0xFFF6F8FC))
            .then(if (selected) Modifier.border(1.5.dp, tint, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = LuTextGray)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (selected) tint else LuTextDark)
    }
}

@Composable
private fun LiveWindDownBlock(plan: SleepPlan, onDelta: (Int) -> Unit, onSet: (Int) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = LiveIndigo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Jeda layar sebelum tidur", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HoldPill(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                background = LiveIndigoSoft,
                onStep = { onDelta(-1) }
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurangi 1 menit", tint = LiveIndigo)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${plan.windDownMinutes} menit", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Text(
                    "Berhenti main layar pukul ${liveFmt(plan.bedMinutes - plan.windDownMinutes)}",
                    fontSize = 12.sp,
                    color = LuTextGray
                )
            }
            HoldPill(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                background = LiveIndigoSoft,
                onStep = { onDelta(1) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah 1 menit", tint = LiveIndigo)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60).forEach { m ->
                val active = plan.windDownMinutes == m
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) LiveIndigo else LiveIndigoSoft)
                        .clickable { onSet(m) }
                        .padding(vertical = 9.dp),
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
    }
}

@Composable
private fun LiveCheckRow(
    checked: Boolean,
    tint: Color,
    title: String,
    subtitle: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveCheck(checked, tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
            Text(subtitle, fontSize = 11.sp, color = LuTextGray)
        }
    }
}

// ---------------------------------------------------------------- DIAL 24 JAM

// Lingkaran 24 jam: 00 di atas, 06 di kanan, 12 di bawah, 18 di kiri.
// Pegangan indigo = waktu tidur, oranye = waktu bangun. Hanya sentuhan di dekat cincin yang menggeser
// pegangan, jadi layar tetap bisa digulir kalau kamu menyentuh bagian lain.
@Composable
private fun SleepDial(
    bed: Int,
    wake: Int,
    active: Int,
    onActive: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    center: @Composable ColumnScope.() -> Unit
) {
    val bedNow by rememberUpdatedState(bed)
    val wakeNow by rememberUpdatedState(wake)
    val onActiveNow by rememberUpdatedState(onActive)
    val onMoveNow by rememberUpdatedState(onMove)
    val duration = liveDuration(bed, wake)

    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val strokePx = 24.dp.toPx()
                    val band = 36.dp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val ringR = (min(size.width, size.height) - strokePx) / 2f
                        val dist = hypot(down.position.x - cx, down.position.y - cy)
                        if (abs(dist - ringR) > band) return@awaitEachGesture
                        down.consume()
                        val first = dialMinutes(down.position, cx, cy)
                        val which = if (circDist(first, bedNow) <= circDist(first, wakeNow)) 0 else 1
                        onActiveNow(which)
                        onMoveNow(which, snap5(first))
                        drag(down.id) { change ->
                            change.consume()
                            onMoveNow(which, snap5(dialMinutes(change.position, cx, cy)))
                        }
                    }
                }
        ) {
            val stroke = 24.dp.toPx()
            val r = (min(size.width, size.height) - stroke) / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            val topLeft = Offset(c.x - r, c.y - r)
            val arcSize = Size(r * 2f, r * 2f)

            drawArc(
                color = LiveDialTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = LiveIndigo.copy(alpha = 0.85f),
                startAngle = bed / 1440f * 360f - 90f,
                sweepAngle = duration / 1440f * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            fun pointAt(m: Int): Offset {
                val rad = Math.toRadians(m / 1440.0 * 360.0 - 90.0)
                return Offset(c.x + r * cos(rad).toFloat(), c.y + r * sin(rad).toFloat())
            }

            fun handle(m: Int, color: Color, isActive: Boolean) {
                val p = pointAt(m)
                val radius = (if (isActive) 17.dp else 13.dp).toPx()
                drawCircle(Color.White, radius = radius, center = p)
                drawCircle(color, radius = radius, center = p, style = Stroke(width = 4.dp.toPx()))
                if (isActive) drawCircle(color, radius = radius * 0.45f, center = p)
            }

            // Pegangan yang aktif digambar terakhir supaya berada di atas.
            if (active == 0) {
                handle(wake, LiveWake, false)
                handle(bed, LiveIndigo, true)
            } else {
                handle(bed, LiveIndigo, false)
                handle(wake, LiveWake, true)
            }
        }

        Text("00", fontSize = 10.sp, color = LuTextGray, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
        Text("06", fontSize = 10.sp, color = LuTextGray, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp))
        Text("12", fontSize = 10.sp, color = LuTextGray, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
        Text("18", fontSize = 10.sp, color = LuTextGray, modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, content = center)
    }
}

// Tombol yang bergeser sekali saat disentuh, lalu terus bergeser (makin cepat) selama ditahan.
@Composable
private fun HoldPill(
    modifier: Modifier,
    background: Color,
    onStep: () -> Unit,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val step by rememberUpdatedState(onStep)
    val scope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    step()
                    val job = scope.launch {
                        delay(420)
                        var n = 0
                        while (true) {
                            step()
                            n++
                            delay(if (n < 12) 90L else 35L)
                        }
                    }
                    tryAwaitRelease()
                    job.cancel()
                })
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ---------------------------------------------------------------- HIDRASI

@Composable
private fun LiveWaterCard(count: Int, target: Int, onOpen: () -> Unit) {
    val progress = if (target == 0) 0f else (count.toFloat() / target).coerceAtMost(1f)
    val percent = (progress * 100).roundToInt()
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
                    Text("Target air putih harian", fontSize = 12.sp, color = LuTextGray)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuLiveBg)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = LuLiveIcon, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Catat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuLiveIcon)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("$count / $target gelas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Text("$percent%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextGray)
            }
            Spacer(Modifier.height(8.dp))
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

// ---------------------------------------------------------------- POLA MAKAN

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
                    Text("Tandai setiap waktu makan", fontSize = 12.sp, color = LuTextGray)
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

// ---------------------------------------------------------------- DIALOG KETIK JAM

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

private fun liveWrap(minutes: Int): Int = ((minutes % 1440) + 1440) % 1440

private fun liveFmt(minutes: Int): String {
    val m = liveWrap(minutes)
    return String.format(Locale.US, "%02d:%02d", m / 60, m % 60)
}

private fun liveDuration(bed: Int, wake: Int): Int =
    if (wake > bed) wake - bed else wake + 1440 - bed

private fun liveDurationShort(duration: Int): String {
    val h = duration / 60
    val m = duration % 60
    return if (m == 0) "$h j" else "$h j $m m"
}

private fun liveDurationLong(duration: Int): String {
    val h = duration / 60
    val m = duration % 60
    return when {
        h == 0 -> "$m menit"
        m == 0 -> "$h jam"
        else -> "$h jam $m menit"
    }
}

// Titik sentuh di dial -> menit (0 sampai 1439), dihitung dari atas searah jarum jam.
private fun dialMinutes(pos: Offset, cx: Float, cy: Float): Int {
    val dx = (pos.x - cx).toDouble()
    val dy = (pos.y - cy).toDouble()
    var deg = Math.toDegrees(atan2(dx, -dy))
    if (deg < 0) deg += 360.0
    return (deg / 360.0 * 1440.0).roundToInt() % 1440
}

private fun snap5(m: Int): Int = ((m + 2) / 5 * 5) % 1440

private fun circDist(a: Int, b: Int): Int {
    val d = abs(a - b)
    return min(d, 1440 - d)
}

// ---------------------------------------------------------------- TANGGAL (periode tracker)

private fun liveParseDate(key: String): Long? =
    runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)?.time }.getOrNull()

private fun liveDaysBetween(fromKey: String, toKey: String): Int {
    val a = liveParseDate(fromKey) ?: return 0
    val b = liveParseDate(toKey) ?: return 0
    return ((b - a) / 86_400_000.0).roundToInt()
}

private fun liveAddDays(key: String, days: Int): String {
    val t = liveParseDate(key) ?: return key
    val cal = Calendar.getInstance()
    cal.timeInMillis = t
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

private fun livePretty(key: String): String {
    val t = liveParseDate(key) ?: return key
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(t))
}
