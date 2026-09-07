package com.sbftrainer.app

data class QuestionStat(
    var timesShown: Int = 0,
    var timesCorrect: Int = 0,
    var timesWrong: Int = 0,
    var lastPracticedAt: Long = 0L,
    var marked: Boolean = false
)
