package com.sbftrainer.app

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

data class AnswerReward(val coinsEarned: Int, val streak: Int, val streakBonus: Boolean)

object ProgressStore {
    private const val FILE_NAME = "progress.json"
    const val COINS_PER_CORRECT = 10
    const val STREAK_BONUS_EVERY = 5
    const val STREAK_BONUS_COINS = 20
    const val HINT_COST = 15

    private val executor = Executors.newSingleThreadExecutor()
    private var loaded = false
    private val stats = HashMap<String, QuestionStat>()
    private var coins = 0
    private var streak = 0
    private lateinit var appContext: Context

    @Synchronized
    fun init(context: Context) {
        if (loaded) return
        appContext = context.applicationContext
        val file = File(appContext.filesDir, FILE_NAME)
        if (file.exists()) {
            try {
                val root = JSONObject(file.readText(Charsets.UTF_8))
                val statsJson = if (root.has("stats")) root.getJSONObject("stats") else root
                val keys = statsJson.keys()
                while (keys.hasNext()) {
                    val id = keys.next()
                    val o = statsJson.getJSONObject(id)
                    stats[id] = QuestionStat(
                        timesShown = o.optInt("timesShown", 0),
                        timesCorrect = o.optInt("timesCorrect", 0),
                        timesWrong = o.optInt("timesWrong", 0),
                        lastPracticedAt = o.optLong("lastPracticedAt", 0L),
                        marked = o.optBoolean("marked", false)
                    )
                }
                coins = root.optInt("coins", 0)
                streak = root.optInt("streak", 0)
            } catch (e: Exception) {
                // Beschädigte Datei ignorieren, mit leerem Fortschritt weitermachen
            }
        }
        loaded = true
    }

    @Synchronized
    fun getStat(id: String): QuestionStat = stats.getOrPut(id) { QuestionStat() }

    @Synchronized
    fun getCoins(): Int = coins

    @Synchronized
    fun getStreak(): Int = streak

    @Synchronized
    fun recordAnswer(id: String, correct: Boolean): AnswerReward {
        val stat = getStat(id)
        stat.timesShown += 1
        if (correct) stat.timesCorrect += 1 else stat.timesWrong += 1
        stat.lastPracticedAt = System.currentTimeMillis()

        var coinsEarned = 0
        var streakBonus = false
        if (correct) {
            streak += 1
            coinsEarned = COINS_PER_CORRECT
            if (streak % STREAK_BONUS_EVERY == 0) {
                coinsEarned += STREAK_BONUS_COINS
                streakBonus = true
            }
            coins += coinsEarned
        } else {
            streak = 0
        }
        persist()
        return AnswerReward(coinsEarned, streak, streakBonus)
    }

    @Synchronized
    fun spendCoins(amount: Int): Boolean {
        if (coins < amount) return false
        coins -= amount
        persist()
        return true
    }

    @Synchronized
    fun toggleMarked(id: String): Boolean {
        val stat = getStat(id)
        stat.marked = !stat.marked
        persist()
        return stat.marked
    }

    @Synchronized
    fun resetAll() {
        stats.clear()
        coins = 0
        streak = 0
        persist()
    }

    private fun persist() {
        val statsJson = JSONObject()
        for ((id, stat) in stats) {
            val o = JSONObject()
            o.put("timesShown", stat.timesShown)
            o.put("timesCorrect", stat.timesCorrect)
            o.put("timesWrong", stat.timesWrong)
            o.put("lastPracticedAt", stat.lastPracticedAt)
            o.put("marked", stat.marked)
            statsJson.put(id, o)
        }
        val root = JSONObject()
        root.put("stats", statsJson)
        root.put("coins", coins)
        root.put("streak", streak)
        val text = root.toString()
        val target = File(appContext.filesDir, FILE_NAME)
        executor.execute {
            try {
                target.writeText(text, Charsets.UTF_8)
            } catch (e: Exception) {
                // Schreiben fehlgeschlagen - Fortschritt bleibt bis zum naechsten Versuch im Speicher
            }
        }
    }
}
