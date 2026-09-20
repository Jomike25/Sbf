package com.sbftrainer.app

import android.content.Context

object QuizSelector {

    const val COUNT_ALL = Int.MAX_VALUE
    const val STALE_THRESHOLD_MILLIS = 3L * 24 * 60 * 60 * 1000
    const val QUICK_COUNT = 10

    fun poolForMode(
        context: Context,
        category: String,
        mode: QuizMode,
        filter: QuestionFilter = QuestionFilter.ALL
    ): List<Question> {
        val all = QuestionRepository.getAll(context, category)
        val byMode = when (mode) {
            QuizMode.ALL, QuizMode.SMART, QuizMode.EXAM -> all
            QuizMode.MARKED -> all.filter { ProgressStore.getStat(it.id).marked }
            QuizMode.WRONG -> all.filter { ProgressStore.getStat(it.id).timesWrong > 0 }
            QuizMode.STALE -> {
                val cutoff = System.currentTimeMillis() - STALE_THRESHOLD_MILLIS
                all.filter {
                    val t = ProgressStore.getStat(it.id).lastPracticedAt
                    t == 0L || t < cutoff
                }
            }
        }
        // Der Pruefungsbogen bleibt immer vollstaendig, sonst waere es kein Bogen mehr
        if (mode == QuizMode.EXAM || filter == QuestionFilter.ALL) return byMode
        return byMode.filter { filter.matches(it) }
    }

    fun examSession(context: Context, category: String, bogenNumber: Int): List<Question> {
        val ids = BogenRepository.questionIdsForBogen(context, category, bogenNumber)
        val byId = QuestionRepository.getAll(context, category).associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    fun availableCount(
        context: Context,
        category: String,
        mode: QuizMode,
        filter: QuestionFilter = QuestionFilter.ALL
    ): Int = poolForMode(context, category, mode, filter).size

    /** Anzahl der Fragen einer Kategorie, auf die dieser Unterkategorie-Filter passt. */
    fun filterCount(context: Context, category: String, filter: QuestionFilter): Int =
        QuestionRepository.getAll(context, category).count { filter.matches(it) }

    fun buildSession(
        context: Context,
        category: String,
        mode: QuizMode,
        requestedCount: Int,
        filter: QuestionFilter = QuestionFilter.ALL
    ): List<Question> {
        val pool = poolForMode(context, category, mode, filter)
        val ordered = when (mode) {
            QuizMode.SMART -> smartOrder(pool)
            QuizMode.WRONG -> pool.sortedByDescending {
                val s = ProgressStore.getStat(it.id)
                s.timesWrong - s.timesCorrect
            }
            QuizMode.STALE -> pool.sortedBy {
                val t = ProgressStore.getStat(it.id).lastPracticedAt
                if (t == 0L) Long.MIN_VALUE else t
            }
            else -> pool.shuffled()
        }
        return if (requestedCount in 1 until ordered.size) ordered.take(requestedCount) else ordered
    }

    /**
     * Mischung fuer den Schnellstart: erst Fragen, die zuletzt Probleme gemacht haben,
     * dann noch nie geuebte, dann lange nicht geuebte, zuletzt der Rest.
     */
    private fun smartOrder(pool: List<Question>): List<Question> {
        val cutoff = System.currentTimeMillis() - STALE_THRESHOLD_MILLIS
        val weak = ArrayList<Question>()
        val fresh = ArrayList<Question>()
        val stale = ArrayList<Question>()
        val rest = ArrayList<Question>()

        for (q in pool) {
            val stat = ProgressStore.getStat(q.id)
            when {
                stat.timesShown == 0 -> fresh.add(q)
                stat.timesWrong > stat.timesCorrect -> weak.add(q)
                stat.lastPracticedAt < cutoff -> stale.add(q)
                else -> rest.add(q)
            }
        }
        return weak.shuffled() + fresh.shuffled() + stale.shuffled() + rest.shuffled()
    }
}
