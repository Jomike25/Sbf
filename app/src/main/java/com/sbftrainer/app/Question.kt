package com.sbftrainer.app

data class Question(
    val id: String,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val hasImage: Boolean,
    val imagePath: String?
)
