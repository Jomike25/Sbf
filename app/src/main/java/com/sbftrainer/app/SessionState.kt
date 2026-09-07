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
    val missed = mutableListOf<Question>()

    fun start(category: String, mode: QuizMode, questions: List<Question>, bogenNumber: Int? = null) {
        this.category = category
        this.mode = mode
        this.questions = questions
        this.correctCount = 0
        this.bogenNumber = bogenNumber
        this.missed.clear()
    }
}
