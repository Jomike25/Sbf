package com.sbftrainer.app

/**
 * Einfache Levelkurve: Level 1 -> 2 kostet 100 XP, jedes weitere Level 50 XP mehr.
 */
object Levels {

    private const val BASE = 100
    private const val STEP = 50

    /** XP-Bedarf, um von [level] auf das naechste Level zu kommen. */
    fun costOfLevel(level: Int): Int = BASE + (level - 1) * STEP

    fun levelFor(xp: Int): Int {
        var level = 1
        var remaining = xp
        while (remaining >= costOfLevel(level)) {
            remaining -= costOfLevel(level)
            level += 1
        }
        return level
    }

    /** XP innerhalb des aktuellen Levels. */
    fun xpIntoLevel(xp: Int): Int {
        var level = 1
        var remaining = xp
        while (remaining >= costOfLevel(level)) {
            remaining -= costOfLevel(level)
            level += 1
        }
        return remaining
    }

    fun xpForNextLevel(xp: Int): Int = costOfLevel(levelFor(xp))

    fun progressPercent(xp: Int): Int {
        val needed = xpForNextLevel(xp)
        if (needed <= 0) return 0
        return (xpIntoLevel(xp) * 100 / needed).coerceIn(0, 100)
    }
}
