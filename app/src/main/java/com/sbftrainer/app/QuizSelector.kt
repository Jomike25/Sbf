package com.sbftrainer.app

import android.content.Context

object QuizSelector {

    const val COUNT_ALL = Int.MAX_VALUE

    fun poolForMode(context: Context, category: String, mode: QuizMode): List<Question> {
        val all = QuestionRepository.getAll(context, category)
        return when (mode) {
            QuizMode.ALL -> all
            QuizMode.MARKED -> all.filter { ProgressStore.getStat(it.id).marked }
            QuizMode.WRONG -> all.filter { ProgressStore.getStat(it.id).timesWrong > 0 }
            QuizMode.STALE -> all
        }
    }

    fun availableCount(context: Context, category: String, mode: QuizMode): Int =
        poolForMode(context, category, mode).size

    fun buildSession(context: Context, category: String, mode: QuizMode, requestedCount: Int): List<Question> {
        val pool = poolForMode(context, category, mode)
        val ordered = when (mode) {
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
}
