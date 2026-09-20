package com.sbftrainer.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executors

/**
 * Haelt alles, was das Lernen spielerischer macht: XP, Level, Tagesstreak,
 * Tagesziel, freigeschaltete Abzeichen und die Aktivitaet der letzten Tage.
 *
 * Bewusst getrennt von [ProgressStore], damit die reine Fragen-Statistik
 * (progress.json) unveraendert bleibt.
 */
object GamificationStore {

    private const val FILE_NAME = "gamification.json"
    private const val HISTORY_DAYS = 30

    const val XP_PER_CORRECT = 10
    const val XP_ROUND_FINISHED = 25
    const val XP_PERFECT_ROUND = 50
    const val XP_EXAM_GOAL = 100

    /** Ab dieser Quote gilt ein Pruefungsbogen in der App als geschafft. */
    const val EXAM_GOAL_PERCENT = 90

    val GOAL_OPTIONS = listOf(10, 20, 30, 50)

    private val executor = Executors.newSingleThreadExecutor()
    private var loaded = false
    private lateinit var appContext: Context

    var xp: Int = 0
        private set
    var totalAnswered: Int = 0
        private set
    var totalCorrect: Int = 0
        private set
    var bestCombo: Int = 0
        private set
    var streakDays: Int = 0
        private set
    var longestStreak: Int = 0
        private set
    var lastActiveDay: Long = 0L
        private set
    var dailyGoal: Int = 20
        private set
    var perfectRounds: Int = 0
        private set
    var soundEnabled: Boolean = true
        private set

    private val history = HashMap<Long, Int>()
    private val goalDays = HashSet<Long>()
    private val examsCompleted = HashSet<String>()
    private val unlocked = HashMap<String, Long>()

    @Synchronized
    fun init(context: Context) {
        if (loaded) return
        appContext = context.applicationContext
        val file = File(appContext.filesDir, FILE_NAME)
        if (file.exists()) {
            try {
                read(JSONObject(file.readText(Charsets.UTF_8)))
            } catch (e: Exception) {
                // Beschaedigte Datei ignorieren und mit leerem Stand weitermachen
            }
        }
        loaded = true
        refreshStreak()
    }

    private fun read(json: JSONObject) {
        xp = json.optInt("xp", 0)
        totalAnswered = json.optInt("totalAnswered", 0)
        totalCorrect = json.optInt("totalCorrect", 0)
        bestCombo = json.optInt("bestCombo", 0)
        streakDays = json.optInt("streakDays", 0)
        longestStreak = json.optInt("longestStreak", 0)
        lastActiveDay = json.optLong("lastActiveDay", 0L)
        dailyGoal = json.optInt("dailyGoal", 20)
        perfectRounds = json.optInt("perfectRounds", 0)
        soundEnabled = json.optBoolean("soundEnabled", true)

        val historyJson = json.optJSONObject("history")
        if (historyJson != null) {
            val keys = historyJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                key.toLongOrNull()?.let { history[it] = historyJson.optInt(key, 0) }
            }
        }
        readLongs(json.optJSONArray("goalDays"))?.let { goalDays.addAll(it) }
        readStrings(json.optJSONArray("examsCompleted"))?.let { examsCompleted.addAll(it) }

        val unlockedJson = json.optJSONObject("unlocked")
        if (unlockedJson != null) {
            val keys = unlockedJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                unlocked[key] = unlockedJson.optLong(key, 0L)
            }
        }
    }

    private fun readLongs(array: JSONArray?): List<Long>? {
        if (array == null) return null
        return (0 until array.length()).map { array.optLong(it, 0L) }.filter { it != 0L }
    }

    private fun readStrings(array: JSONArray?): List<String>? {
        if (array == null) return null
        return (0 until array.length()).mapNotNull { array.optString(it, null) }
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    /**
     * Beendet den Streak, wenn ein ganzer Tag ohne Uebung vergangen ist.
     * Gestern geuebt heisst: der Streak lebt noch und kann heute fortgesetzt werden.
     */
    @Synchronized
    fun refreshStreak() {
        if (lastActiveDay == 0L) return
        val gap = today() - lastActiveDay
        if (gap > 1 && streakDays != 0) {
            streakDays = 0
            persist()
        }
    }

    @Synchronized
    fun answeredToday(): Int = history[today()] ?: 0

    @Synchronized
    fun goalReachedToday(): Boolean = answeredToday() >= dailyGoal

    @Synchronized
    fun goalDaysReached(): Int = goalDays.size

    @Synchronized
    fun examsCompletedCount(category: String): Int =
        examsCompleted.count { it.startsWith("$category-") }

    @Synchronized
    fun isExamCompleted(category: String, bogenNumber: Int): Boolean =
        examsCompleted.contains("$category-$bogenNumber")

    @Synchronized
    fun unlockedIds(): Set<String> = unlocked.keys.toSet()

    @Synchronized
    fun unlockedAt(id: String): Long? = unlocked[id]

    /** Aktivitaet der letzten [days] Tage, aeltester Tag zuerst. */
    @Synchronized
    fun recentActivity(days: Int): List<Pair<Long, Int>> {
        val today = today()
        return (days - 1 downTo 0).map { offset ->
            val day = today - offset
            day to (history[day] ?: 0)
        }
    }

    @Synchronized
    fun setDailyGoal(goal: Int) {
        dailyGoal = goal
        if (goalReachedToday()) goalDays.add(today())
        persist()
    }

    @Synchronized
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        persist()
    }

    /** Zaehlt eine Antwort, pflegt Streak/Tagesziel und gibt die verdienten XP zurueck. */
    @Synchronized
    fun recordAnswer(correct: Boolean, combo: Int): Int {
        val today = today()
        if (lastActiveDay != today) {
            streakDays = if (lastActiveDay != 0L && today - lastActiveDay == 1L) streakDays + 1 else 1
            lastActiveDay = today
            if (streakDays > longestStreak) longestStreak = streakDays
        }

        totalAnswered += 1
        history[today] = (history[today] ?: 0) + 1
        trimHistory()

        var earned = 0
        if (correct) {
            totalCorrect += 1
            earned = XP_PER_CORRECT + comboBonus(combo)
            xp += earned
            if (combo > bestCombo) bestCombo = combo
        }

        if (goalReachedToday()) goalDays.add(today)
        persist()
        return earned
    }

    /** Bonus-XP fuer eine Serie richtiger Antworten, gedeckelt bei +10. */
    fun comboBonus(combo: Int): Int = when {
        combo < 3 -> 0
        else -> minOf((combo - 2) * 2, 10)
    }

    /** Schliesst eine Runde ab und gibt die Bonus-XP zurueck. */
    @Synchronized
    fun recordRoundFinished(
        total: Int,
        correct: Int,
        mode: QuizMode,
        category: String,
        bogenNumber: Int?
    ): Int {
        if (total <= 0) return 0
        var bonus = XP_ROUND_FINISHED
        val perfect = correct == total
        if (perfect && total >= 10) {
            perfectRounds += 1
            bonus += XP_PERFECT_ROUND
        }
        if (mode == QuizMode.EXAM && bogenNumber != null) {
            examsCompleted.add("$category-$bogenNumber")
            if (correct * 100 / total >= EXAM_GOAL_PERCENT) bonus += XP_EXAM_GOAL
        }
        xp += bonus
        persist()
        return bonus
    }

    /** Merkt neu erreichte Abzeichen und gibt genau die neuen zurueck. */
    @Synchronized
    fun unlock(ids: List<String>): List<String> {
        val fresh = ids.filter { !unlocked.containsKey(it) }
        if (fresh.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        fresh.forEach { unlocked[it] = now }
        persist()
        return fresh
    }

    @Synchronized
    fun resetAll() {
        xp = 0
        totalAnswered = 0
        totalCorrect = 0
        bestCombo = 0
        streakDays = 0
        longestStreak = 0
        lastActiveDay = 0L
        perfectRounds = 0
        history.clear()
        goalDays.clear()
        examsCompleted.clear()
        unlocked.clear()
        persist()
    }

    private fun trimHistory() {
        val cutoff = today() - HISTORY_DAYS
        history.keys.filter { it < cutoff }.forEach { history.remove(it) }
    }

    private fun persist() {
        val json = JSONObject()
        json.put("xp", xp)
        json.put("totalAnswered", totalAnswered)
        json.put("totalCorrect", totalCorrect)
        json.put("bestCombo", bestCombo)
        json.put("streakDays", streakDays)
        json.put("longestStreak", longestStreak)
        json.put("lastActiveDay", lastActiveDay)
        json.put("dailyGoal", dailyGoal)
        json.put("perfectRounds", perfectRounds)
        json.put("soundEnabled", soundEnabled)

        val historyJson = JSONObject()
        for ((day, count) in history) historyJson.put(day.toString(), count)
        json.put("history", historyJson)

        json.put("goalDays", JSONArray(goalDays.toList()))
        json.put("examsCompleted", JSONArray(examsCompleted.toList()))

        val unlockedJson = JSONObject()
        for ((id, at) in unlocked) unlockedJson.put(id, at)
        json.put("unlocked", unlockedJson)

        val text = json.toString()
        val target = File(appContext.filesDir, FILE_NAME)
        executor.execute {
            try {
                target.writeText(text, Charsets.UTF_8)
            } catch (e: Exception) {
                // Schreiben fehlgeschlagen - Stand bleibt bis zum naechsten Versuch im Speicher
            }
        }
    }
}
