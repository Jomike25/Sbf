package com.sbftrainer.app

import android.content.Context

/**
 * Ein Abzeichen. [progress] liefert (erreicht, Ziel) - sobald erreicht >= Ziel ist,
 * gilt das Abzeichen als geschafft und wird beim naechsten Runden-Ende freigeschaltet.
 */
data class Achievement(
    val id: String,
    val emoji: String,
    val titleRes: Int,
    val descRes: Int,
    val progress: (Context) -> Pair<Int, Int>
)

object Achievements {

    private const val EXAM_TARGET_FALLBACK = 15

    val all: List<Achievement> = listOf(
        Achievement("first_steps", "🌱", R.string.ach_first_steps, R.string.ach_first_steps_desc) {
            GamificationStore.totalAnswered to 10
        },
        Achievement("hundred", "💯", R.string.ach_hundred, R.string.ach_hundred_desc) {
            GamificationStore.totalAnswered to 100
        },
        Achievement("five_hundred", "🚢", R.string.ach_five_hundred, R.string.ach_five_hundred_desc) {
            GamificationStore.totalAnswered to 500
        },
        Achievement("combo_10", "🔥", R.string.ach_combo_10, R.string.ach_combo_10_desc) {
            GamificationStore.bestCombo to 10
        },
        Achievement("combo_25", "⚡", R.string.ach_combo_25, R.string.ach_combo_25_desc) {
            GamificationStore.bestCombo to 25
        },
        Achievement("streak_3", "📅", R.string.ach_streak_3, R.string.ach_streak_3_desc) {
            GamificationStore.longestStreak to 3
        },
        Achievement("streak_7", "🗓", R.string.ach_streak_7, R.string.ach_streak_7_desc) {
            GamificationStore.longestStreak to 7
        },
        Achievement("streak_30", "🏆", R.string.ach_streak_30, R.string.ach_streak_30_desc) {
            GamificationStore.longestStreak to 30
        },
        Achievement("perfect_round", "✨", R.string.ach_perfect_round, R.string.ach_perfect_round_desc) {
            GamificationStore.perfectRounds to 1
        },
        Achievement("goal_day", "🎯", R.string.ach_goal_day, R.string.ach_goal_day_desc) {
            GamificationStore.goalDaysReached() to 1
        },
        Achievement("goal_7", "🎖", R.string.ach_goal_7, R.string.ach_goal_7_desc) {
            GamificationStore.goalDaysReached() to 7
        },
        Achievement("exam_5", "📘", R.string.ach_exam_5, R.string.ach_exam_5_desc) {
            examsCompleted() to 5
        },
        Achievement("exam_all", "🧭", R.string.ach_exam_all, R.string.ach_exam_all_desc) { context ->
            examsCompleted() to examTarget(context)
        },
        Achievement("accuracy_90", "🎓", R.string.ach_accuracy_90, R.string.ach_accuracy_90_desc) {
            val answered = GamificationStore.totalAnswered
            val accuracy = if (answered >= 100) GamificationStore.totalCorrect * 100 / answered else 0
            accuracy to 90
        }
    )

    private fun examsCompleted(): Int = maxOf(
        GamificationStore.examsCompletedCount(QuestionRepository.CATEGORY_BINNEN),
        GamificationStore.examsCompletedCount(QuestionRepository.CATEGORY_SEE)
    )

    private fun examTarget(context: Context): Int = try {
        BogenRepository.bogenNumbers(context, QuestionRepository.CATEGORY_BINNEN).size
            .takeIf { it > 0 } ?: EXAM_TARGET_FALLBACK
    } catch (e: Exception) {
        EXAM_TARGET_FALLBACK
    }

    fun byId(id: String): Achievement? = all.firstOrNull { it.id == id }

    fun isUnlocked(id: String): Boolean = GamificationStore.unlockedIds().contains(id)

    fun unlockedCount(): Int = all.count { isUnlocked(it.id) }

    /** Prueft alle Abzeichen und gibt die in diesem Moment neu erreichten zurueck. */
    fun checkForNew(context: Context): List<Achievement> {
        val satisfied = all.filter { achievement ->
            val (current, target) = achievement.progress(context)
            current >= target
        }
        val freshIds = GamificationStore.unlock(satisfied.map { it.id })
        return freshIds.mapNotNull { byId(it) }
    }
}
