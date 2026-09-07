package com.sbftrainer.app

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

object ProgressStore {
    private const val FILE_NAME = "progress.json"
    private val executor = Executors.newSingleThreadExecutor()
    private var loaded = false
    private val stats = HashMap<String, QuestionStat>()
    private lateinit var appContext: Context

    @Synchronized
    fun init(context: Context) {
        if (loaded) return
        appContext = context.applicationContext
        val file = File(appContext.filesDir, FILE_NAME)
        if (file.exists()) {
            try {
                val json = JSONObject(file.readText(Charsets.UTF_8))
                val keys = json.keys()
                while (keys.hasNext()) {
                    val id = keys.next()
                    val o = json.getJSONObject(id)
                    stats[id] = QuestionStat(
                        timesShown = o.optInt("timesShown", 0),
                        timesCorrect = o.optInt("timesCorrect", 0),
                        timesWrong = o.optInt("timesWrong", 0),
                        lastPracticedAt = o.optLong("lastPracticedAt", 0L),
                        marked = o.optBoolean("marked", false)
                    )
                }
            } catch (e: Exception) {
                // Beschädigte Datei ignorieren, mit leerem Fortschritt weitermachen
            }
        }
        loaded = true
    }

    @Synchronized
    fun getStat(id: String): QuestionStat = stats.getOrPut(id) { QuestionStat() }

    @Synchronized
    fun recordAnswer(id: String, correct: Boolean) {
        val stat = getStat(id)
        stat.timesShown += 1
        if (correct) stat.timesCorrect += 1 else stat.timesWrong += 1
        stat.lastPracticedAt = System.currentTimeMillis()
        persist()
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
        persist()
    }

    private fun persist() {
        val snapshot = JSONObject()
        for ((id, stat) in stats) {
            val o = JSONObject()
            o.put("timesShown", stat.timesShown)
            o.put("timesCorrect", stat.timesCorrect)
            o.put("timesWrong", stat.timesWrong)
            o.put("lastPracticedAt", stat.lastPracticedAt)
            o.put("marked", stat.marked)
            snapshot.put(id, o)
        }
        val text = snapshot.toString()
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
