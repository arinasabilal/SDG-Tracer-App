// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/ui/WaterScreen.kt
// Layar Minum Air Putih (Up Live): catat gelas, atur target harian dan periode.
// Saat jumlah gelas mencapai target, kebiasaan "water" di Beranda otomatis ditandai selesai.
package com.example.apk.ui

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.data.HabitRepository
import com.example.apk.data.WaterPlan
import com.example.apk.data.WaterRepository
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MAX_GLASSES = 40
private const val MAX_TARGET = 30

private val WaterTrack = Color(0xFFD6E6FA)
private val WaterEmpty = Color(0xFFE9F1FC)
private val WaterEmptyBorder = Color(0xFFC5D8F2)

private val glassShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomEnd = 14.dp, bottomStart = 14.dp)

@Composable
fun WaterScreen(todayKey: String, onBack: () -> Unit) {
    var count by remember { mutableIntStateOf(0) }
    var plan by remember { mutableStateOf<WaterPlan?>(null) }
    // -1 = belum diubah, ikut nilai yang tersimpan.
    var draftTarget by rememberSaveable { mutableIntStateOf(-1) }
    var draftPeriod by rememberSaveable { mutableIntStateOf(-1) }

    DisposableEffect(todayKey) {
        val l1 = WaterRepository.observeWater(todayKey) { count = it }
        val l2 = WaterRepository.observePlan { plan = it }
        onDispose {
            l1?.remove()
            l2?.remove()
        }
    }

    val currentPlan = plan
    val target = currentPlan?.targetGlasses ?: WaterRepository.DEFAULT_TARGET
    val periodDays = currentPlan?.periodDays ?: 0
    val shownTarget = if (draftTarget >= 0) draftTarget else target
    val shownPeriod = if (draftPeriod >= 0) draftPeriod else periodDays
    val dirty = currentPlan == null || shownTarget != target || shownPeriod != periodDays

    // Setiap perubahan jumlah gelas disimpan, dan kebiasaan "water" ikut diperbarui.
    fun changeCount(newCount: Int) {
        val c = newCount.coerceIn(0, MAX_GLASSES)
        count = c
        WaterRepository.setWater(todayKey, c)
        HabitRepository.setDone(todayKey, "water", c >= target)
    }

    val progress by animateFloatAsState(
        if (target == 0) 0f else (count.toFloat() / target).coerceAtMost(1f),
        tween(500),
        label = "waterProgress"
    )

    val message = when {
        count == 0 -> "Yuk, mulai dengan segelas air."
        count < target -> "Tinggal ${target - count} gelas lagi."
        count == target -> "Target hari ini tercapai! 🎉"
        else -> "Mantap, melebihi target hari ini!"
    }

    val periodInfo = if (shownPeriod == 0) {
        "Berlaku tanpa batas waktu"
    } else {
        val start = if (currentPlan != null && currentPlan.periodDays == shownPeriod) currentPlan.startDate else todayKey
        "Berlaku sampai ${wPretty(wAddDays(start, shownPeriod - 1))}"
    }

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
                        Text("Minum Air Putih", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Text(
                            "Atur target minum harianmu",
                            fontSize = 13.sp,
                            color = LuTextDark.copy(alpha = 0.72f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                WaterProgressCard(count, target, progress, message)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Catat gelasmu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(2.dp))
            Text("Ketuk gelas, atau pakai tombol di bawah.", fontSize = 12.sp, color = LuTextGray)
            Spacer(Modifier.height(12.dp))

            val shownGlasses = maxOf(target, count)
            (0 until shownGlasses).toList().chunked(5).forEach { rowIdx ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowIdx.forEach { i ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            GlassCell(filled = i < count) {
                                // Mengetuk gelas terisi yang terakhir = membatalkannya.
                                changeCount(if (i + 1 == count) i else i + 1)
                            }
                        }
                    }
                    repeat(5 - rowIdx.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepButton("−", enabled = count > 0) { changeCount(count - 1) }
                Button(
                    onClick = { changeCount(count + 1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuLiveIcon)
                ) {
                    Text("💧  Tambah segelas", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LuSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Target harian", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Berapa gelas air yang ingin kamu minum setiap hari?",
                        fontSize = 12.sp,
                        color = LuTextGray
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepButton("−", enabled = shownTarget > 1) { draftTarget = shownTarget - 1 }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("$shownTarget", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                            Text("gelas/hari", fontSize = 12.sp, color = LuTextGray)
                        }
                        StepButton("+", enabled = shownTarget < MAX_TARGET) { draftTarget = shownTarget + 1 }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("Periode target", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7 to "1 minggu", 30 to "1 bulan", 0 to "Tak terbatas").forEach { (days, label) ->
                            val active = shownPeriod == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (active) LuLiveIcon else LuLiveBg)
                                    .clickable { draftPeriod = days }
                                    .padding(vertical = 12.dp),
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
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(periodInfo, fontSize = 12.sp, color = LuTextGray)

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            val start = if (currentPlan != null && currentPlan.periodDays == shownPeriod) {
                                currentPlan.startDate
                            } else {
                                todayKey
                            }
                            WaterRepository.savePlan(shownTarget, shownPeriod, start)
                            HabitRepository.setDone(todayKey, "water", count >= shownTarget)
                        },
                        enabled = dirty,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LuLiveIcon)
                    ) {
                        Text(if (dirty) "Simpan" else "Tersimpan ✓", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------- KOMPONEN

@Composable
private fun WaterProgressCard(count: Int, target: Int, progress: Float, message: String) {
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
                        color = WaterTrack,
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
                Text("$count/$target", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Progres hari ini", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LuLiveIcon)
                Spacer(Modifier.height(2.dp))
                Text("$count dari $target gelas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(2.dp))
                Text(message, fontSize = 12.sp, color = LuTextGray)
            }
        }
    }
}

@Composable
private fun GlassCell(filled: Boolean, onClick: () -> Unit) {
    val fill by animateColorAsState(if (filled) LuLiveIcon else WaterEmpty, label = "glass")
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(56.dp)
            .clip(glassShape)
            .background(fill)
            .border(2.dp, if (filled) LuLiveIcon else WaterEmptyBorder, glassShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (filled) Text("💧", fontSize = 18.sp)
    }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(1.5.dp, if (enabled) LuLiveIcon else LuBorder)
    ) {
        Text(
            label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) LuLiveIcon else LuTextGray
        )
    }
}

// ---------------------------------------------------------------- TANGGAL

private fun wParse(key: String): Long? =
    runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)?.time }.getOrNull()

private fun wAddDays(key: String, days: Int): String {
    val t = wParse(key) ?: return key
    val cal = Calendar.getInstance()
    cal.timeInMillis = t
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

private fun wPretty(key: String): String {
    val t = wParse(key) ?: return key
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(t))
}
