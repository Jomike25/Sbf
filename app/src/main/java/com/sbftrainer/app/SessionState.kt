package com.sbftrainer.app

/**
 * Haelt die aktuell laufende Trainingsrunde im Speicher, damit QuizActivity und
 * ResultActivity sie teilen koennen, ohne Question-Objekte per Intent zu reichen.
 */
object SessionState {
    var category: String = ""
    var mode: QuizMode = QuizMode.ALL
    var questions: List<Question> = emptyList()
    var correctCount: Int = 0
    var bogenNumber: Int? = null
    var filter: QuestionFilter = QuestionFilter.ALL
    val missed = mutableListOf<Question>()

    /** Laufende Serie richtiger Antworten und die beste Serie dieser Runde. */
    var combo: Int = 0
    var bestCombo: Int = 0

    /** XP aus einzelnen Antworten bzw. aus dem Rundenabschluss. */
    var xpFromAnswers: Int = 0
    var bonusXp: Int = 0

    var startedAt: Long = 0L
    var durationMillis: Long = 0L

    /** Level vor der Runde, damit das Ergebnis einen Levelaufstieg zeigen kann. */
    var levelBefore: Int = 1

    /** Bereits abgerechnet? Verhindert doppelte XP, wenn ResultActivity neu aufgebaut wird. */
    var settled: Boolean = false

    var newAchievements: List<Achievement> = emptyList()

    fun start(
        category: String,
        mode: QuizMode,
        questions: List<Question>,
        bogenNumber: Int? = null,
        filter: QuestionFilter = QuestionFilter.ALL
    ) {
        this.category = category
        this.mode = mode
        this.questions = questions
        this.correctCount = 0
        this.bogenNumber = bogenNumber
        this.filter = filter
        this.missed.clear()
        this.combo = 0
        this.bestCombo = 0
        this.xpFromAnswers = 0
        this.bonusXp = 0
        this.startedAt = System.currentTimeMillis()
        this.durationMillis = 0L
        this.levelBefore = Levels.levelFor(GamificationStore.xp)
        this.settled = false
        this.newAchievements = emptyList()
    }

    /**
     * Startet eine Wiederholungsrunde mit genau den Fragen, die eben falsch waren.
     * Laeuft bewusst als WRONG-Runde ohne Bogennummer, damit eine Wiederholung nicht
     * als abgeschlossener Pruefungsbogen gezaehlt wird.
     */
    fun startRetryOfMissed(): Boolean {
        val retry = missed.toList()
        if (retry.isEmpty()) return false
        start(category, QuizMode.WRONG, retry, null, filter)
        return true
    }

    fun totalXp(): Int = xpFromAnswers + bonusXp
}
