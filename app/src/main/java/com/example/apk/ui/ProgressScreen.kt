// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/ui/ProgressScreen.kt
// Layar tab Progres = "Sistem Akuntabilitas & Evaluasi" (SDLC Sprint 4):
//   - Daily Streak & Discipline Score (skor otomatis dari 3 modul: Up Live, Up Save, Up Task)
//   - Weekly Discipline Report (grafik 7 hari, rata-rata, perbandingan minggu lalu, skor per modul)
// Tidak menambah koleksi Firestore baru: semua dihitung dari "habits" dan "days" yang sudah ada.
package com.example.apk.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.data.Habit
import com.example.apk.data.HabitModule
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBookBg
import com.example.apk.ui.theme.LuBookIcon
import com.example.apk.ui.theme.LuLiveBg
import com.example.apk.ui.theme.LuLiveIcon
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuSaveBg
import com.example.apk.ui.theme.LuSaveIcon
import com.example.apk.ui.theme.LuSky
import com.example.apk.ui.theme.LuSkySoft
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTaskBg
import com.example.apk.ui.theme.LuTaskIcon
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.example.apk.ui.theme.LuTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- ATURAN HITUNG

// Hari dianggap "disiplin" (menyambung streak) kalau skor harian minimal segini.
internal const val STREAK_THRESHOLD = 50

// Berapa hari ke belakang yang dihitung (butuh >= 14 untuk bandingkan dengan minggu lalu).
const val PROGRESS_LOOKBACK_DAYS = 60

internal val STREAK_MILESTONES = listOf(3, 7, 14, 30)

internal data class DayScore(val key: String, val label: String, val percent: Int)

internal data class ProgressStats(
    val week: List<DayScore>,                    // 7 hari, terlama -> hari ini
    val weekAvg: Int,                            // rata-rata skor 7 hari (0..100)
    val prevWeekAvg: Int?,                       // null kalau belum ada data minggu lalu
    val currentStreak: Int,
    val bestStreak: Int,
    val modulePercent: Map<HabitModule, Int>,    // skor per modul, 7 hari
    val activeDays: Int                          // jumlah hari dengan skor >= STREAK_THRESHOLD (7 hari)
)

private fun dayLabel(key: String): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = runCatching { fmt.parse(key) }.getOrNull() ?: return key
    return SimpleDateFormat("EEE", Locale.forLanguageTag("id-ID")).format(date)
}

// Daftar kunci tanggal, indeks 0 = hari ini, 1 = kemarin, dst.
internal fun keysBack(todayKey: String, count: Int): List<String> {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val start = runCatching { fmt.parse(todayKey) }.getOrNull() ?: Date()
    val cal = Calendar.getInstance().apply { time = start }
    val out = ArrayList<String>(count)
    repeat(count) {
        out.add(fmt.format(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return out
}

internal fun computeProgress(
    habits: List<Habit>,
    history: Map<String, Set<String>>,
    done: Set<String>,
    todayKey: String
): ProgressStats {
    val keys = keysBack(todayKey, PROGRESS_LOOKBACK_DAYS)
    // Hari ini selalu pakai data paling baru (done), sisanya dari riwayat.
    val merged = history + (todayKey to done)

    fun doneOn(key: String): Set<String> = merged[key] ?: emptySet()
    fun percentOn(key: String): Int =
        if (habits.isEmpty()) 0 else habits.count { it.id in doneOn(key) } * 100 / habits.size

    val pcts = keys.map { percentOn(it) }

    val week = (6 downTo 0).map { i -> DayScore(keys[i], dayLabel(keys[i]), pcts[i]) }
    val weekAvg = pcts.take(7).sum() / 7

    val prevHasData = keys.subList(7, 14).any { merged.containsKey(it) }
    val prevAvg = if (prevHasData) pcts.subList(7, 14).sum() / 7 else null

    // Streak saat ini: hari ini yang belum memenuhi syarat tidak memutus streak (hari masih berjalan).
    var current = 0
    var i = if (pcts[0] >= STREAK_THRESHOLD) 0 else 1
    while (i < pcts.size && pcts[i] >= STREAK_THRESHOLD) {
        current++
        i++
    }
    // Streak terbaik dalam rentang yang dihitung.
    var best = 0
    var run = 0
    for (idx in pcts.indices.reversed()) {
        if (pcts[idx] >= STREAK_THRESHOLD) {
            run++
            if (run > best) best = run
        } else {
            run = 0
        }
    }

    val moduleMap = HabitModule.entries.associateWith { module ->
        val items = habits.filter { it.module == module }
        if (items.isEmpty()) {
            0
        } else {
            val doneSum = keys.take(7).sumOf { k -> items.count { it.id in doneOn(k) } }
            doneSum * 100 / (items.size * 7)
        }
    }

    return ProgressStats(
        week = week,
        weekAvg = weekAvg,
        prevWeekAvg = prevAvg,
        currentStreak = current,
        bestStreak = best,
        modulePercent = moduleMap,
        activeDays = pcts.take(7).count { it >= STREAK_THRESHOLD }
    )
}

// ---------------------------------------------------------------- HELPER TAMPILAN

private fun progModuleTitle(m: HabitModule) = when (m) {
    HabitModule.HEALTH -> "Up Live"
    HabitModule.SANITATION -> "Up Save"
    HabitModule.EDUCATION -> "Up Task"
}

private fun progModuleDesc(m: HabitModule) = when (m) {
    HabitModule.HEALTH -> "Tidur, minum air, makan teratur"
    HabitModule.SANITATION -> "Matikan perangkat elektronik"
    HabitModule.EDUCATION -> "Belajar fokus dan tugas"
}

private fun progModuleColors(m: HabitModule): Pair<Color, Color> = when (m) {
    HabitModule.HEALTH -> LuLiveBg to LuLiveIcon
    HabitModule.SANITATION -> LuSaveBg to LuSaveIcon
    HabitModule.EDUCATION -> LuTaskBg to LuTaskIcon
}

private fun insightText(stats: ProgressStats, habits: List<Habit>): String {
    if (habits.isEmpty()) return "Menyiapkan rutinitasmu..."
    if (stats.weekAvg == 0 && stats.currentStreak == 0) {
        return "Belum ada data minggu ini. Centang satu rutinitas hari ini untuk memulai streak."
    }
    val present = HabitModule.entries.filter { m -> habits.any { it.module == m } }
    val weakest = present.minByOrNull { stats.modulePercent[it] ?: 0 }
    val weakestPct = weakest?.let { stats.modulePercent[it] } ?: 0
    return when {
        stats.weekAvg >= 85 -> "Konsistensi minggu ini luar biasa. Pertahankan ritmenya!"
        weakest != null && weakestPct < 50 -> when (weakest) {
            HabitModule.HEALTH -> "Up Live masih rendah. Coba mulai dari satu kebiasaan: jam tidur atau minum air."
            HabitModule.SANITATION -> "Up Save masih rendah. Biasakan cek perangkat sebelum berangkat atau tidur."
            HabitModule.EDUCATION -> "Up Task masih rendah. Satu sesi fokus 25 menit sehari sudah cukup untuk mulai."
        }
        else -> "Sudah cukup baik. Naikkan sedikit demi sedikit, targetkan streak berikutnya."
    }
}

private fun deltaText(avg: Int, prev: Int?): Pair<String, Color> {
    if (prev == null) return "Belum ada pembanding minggu lalu" to LuTextGray
    val diff = avg - prev
    return when {
        diff > 0 -> "▲ naik $diff% dari minggu lalu" to LuSaveIcon
        diff < 0 -> "▼ turun ${-diff}% dari minggu lalu" to Color(0xFFD32F2F)
        else -> "Sama seperti minggu lalu" to LuTextGray
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ---------------------------------------------------------------- LAYAR PROGRES

@Composable
fun ProgressScreen(
    habits: List<Habit>,
    history: Map<String, Set<String>>,
    done: Set<String>,
    todayKey: String
) {
    val stats = remember(habits, history, done, todayKey) {
        computeProgress(habits, history, done, todayKey)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
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
                Text("Laporan mingguan", fontSize = 14.sp, color = LuTextGray)
                Text("Progres", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Streak
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StreakCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🔥",
                    value = stats.currentStreak,
                    caption = "Streak saat ini"
                )
                StreakCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🏆",
                    value = stats.bestStreak,
                    caption = "Streak terbaik"
                )
            }

            // Skor mingguan
            SectionCard("Skor kedisiplinan 7 hari") {
                val (deltaMsg, deltaColor) = deltaText(stats.weekAvg, stats.prevWeekAvg)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${stats.weekAvg}%",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(deltaMsg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = deltaColor)
                        Text(
                            "${stats.activeDays} dari 7 hari memenuhi target ($STREAK_THRESHOLD%+)",
                            fontSize = 12.sp,
                            color = LuTextGray
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                WeekBarChart(stats.week)
            }

            // Skor per modul
            SectionCard("Per modul minggu ini") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HabitModule.entries.forEach { module ->
                        if (habits.any { it.module == module }) {
                            ModuleBar(module, stats.modulePercent[module] ?: 0)
                        }
                    }
                    if (habits.isEmpty()) {
                        Text("Menyiapkan rutinitasmu...", fontSize = 13.sp, color = LuTextGray)
                    }
                }
            }

            // Evaluasi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LuSkySoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    Text("💡", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Evaluasi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                        Spacer(Modifier.height(4.dp))
                        Text(insightText(stats, habits), fontSize = 13.sp, color = LuTextDark)
                    }
                }
            }

            // Pencapaian streak
            SectionCard("Pencapaian streak") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    STREAK_MILESTONES.forEach { days ->
                        MilestoneBadge(
                            modifier = Modifier.weight(1f),
                            days = days,
                            unlocked = stats.bestStreak >= days
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StreakCard(modifier: Modifier, emoji: String, value: Int, caption: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "$value hari",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LuTextDark
            )
            Text(caption, fontSize = 12.sp, color = LuTextGray)
        }
    }
}

@Composable
private fun WeekBarChart(week: List<DayScore>) {
    val chartHeight = 110.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        week.forEachIndexed { index, day ->
            val isToday = index == week.lastIndex
            val fraction by animateFloatAsState(day.percent / 100f, tween(600), label = "bar")
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${day.percent}", fontSize = 10.sp, color = LuTextGray)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuTrack.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight * fraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToday) LuPrimary else LuSky)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    day.label,
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) LuTextDark else LuTextGray
                )
            }
        }
    }
}

@Composable
private fun ModuleBar(module: HabitModule, percent: Int) {
    val (bg, fg) = progModuleColors(module)
    val progress by animateFloatAsState(percent / 100f, tween(600), label = "moduleBar")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(fg)
                )
                Spacer(Modifier.width(8.dp))
                Text(progModuleTitle(module), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
            }
            Text("$percent%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = fg)
        }
        Text(progModuleDesc(module), fontSize = 11.sp, color = LuTextGray)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = fg,
            trackColor = bg
        )
    }
}

@Composable
private fun MilestoneBadge(modifier: Modifier, days: Int, unlocked: Boolean) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (unlocked) LuBookBg else LuTrack.copy(alpha = 0.25f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (unlocked) "🏅" else "🔒", fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "$days hari",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (unlocked) LuBookIcon else LuTextGray
        )
    }
}
