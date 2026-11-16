package com.example.apk

import com.example.apk.data.Habit
import com.example.apk.data.HabitModule
import com.example.apk.ui.computeProgress
import com.example.apk.ui.keysBack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressStatsTest {

    private val habits = listOf(
        Habit("sleep", "Tidur", HabitModule.HEALTH),
        Habit("water", "Air", HabitModule.HEALTH),
        Habit("save", "Matikan perangkat", HabitModule.SANITATION),
        Habit("focus", "Fokus", HabitModule.EDUCATION)
    )
    private val today = "2026-11-16"
    private val keys = keysBack(today, 60)

    @Test
    fun emptyHistory_givesZeroEverything() {
        val s = computeProgress(habits, emptyMap(), emptySet(), today)
        assertEquals(0, s.weekAvg)
        assertEquals(0, s.currentStreak)
        assertEquals(0, s.bestStreak)
        assertNull(s.prevWeekAvg)
        assertEquals(7, s.week.size)
    }

    @Test
    fun todayScore_usesDoneSet_notHistory() {
        val s = computeProgress(habits, emptyMap(), setOf("sleep", "water"), today)
        assertEquals(50, s.week.last().percent)
        assertEquals(1, s.currentStreak)
    }

    @Test
    fun streak_notBrokenByUnfinishedToday() {
        // Kemarin dan 2 hari lalu penuh, hari ini masih kosong -> streak tetap 2.
        val hist = mapOf(
            keys[1] to setOf("sleep", "water", "save", "focus"),
            keys[2] to setOf("sleep", "water", "save", "focus")
        )
        val s = computeProgress(habits, hist, emptySet(), today)
        assertEquals(2, s.currentStreak)
        assertEquals(2, s.bestStreak)
    }

    @Test
    fun streak_brokenByGap_andBestKept() {
        val full = setOf("sleep", "water", "save", "focus")
        val hist = mapOf(
            keys[0] to full,
            keys[1] to full,
            // keys[2] kosong -> putus
            keys[3] to full,
            keys[4] to full,
            keys[5] to full
        )
        val s = computeProgress(habits, hist, full, today)
        assertEquals(2, s.currentStreak)
        assertEquals(3, s.bestStreak)
    }

    @Test
    fun moduleAndDelta_computed() {
        val full = setOf("sleep", "water", "save", "focus")
        val hist = (0..6).associate { keys[it] to full } +
            (7..13).associate { keys[it] to setOf("sleep") } // 25% minggu lalu
        val s = computeProgress(habits, hist, full, today)
        assertEquals(100, s.weekAvg)
        assertEquals(25, s.prevWeekAvg)
        assertEquals(100, s.modulePercent[HabitModule.HEALTH])
        assertEquals(7, s.activeDays)
    }
}
