package com.sbftrainer.app

/**
 * Unterkategorie-Filter fuer die Fragenauswahl: entweder alles, nur Bildfragen
 * oder ein thematisches Unterthema aus [QuestionTopic].
 */
enum class QuestionFilter(
    val emoji: String,
    val labelRes: Int,
    val topic: QuestionTopic?
) {
    ALL("🎯", R.string.filter_all, null),
    IMAGES("🖼", R.string.filter_images, null),
    IMAGES_WITH_FILE("📷", R.string.filter_images_with_file, null),
    NOTFALL(QuestionTopic.NOTFALL),
    ZEICHEN(QuestionTopic.ZEICHEN),
    LICHTER(QuestionTopic.LICHTER),
    VERHALTEN(QuestionTopic.VERHALTEN),
    WETTER(QuestionTopic.WETTER),
    NAVIGATION(QuestionTopic.NAVIGATION),
    TECHNIK(QuestionTopic.TECHNIK),
    SEEMANNSCHAFT(QuestionTopic.SEEMANNSCHAFT),
    RECHT(QuestionTopic.RECHT),
    GRUNDLAGEN(QuestionTopic.GRUNDLAGEN),
    SONSTIGE(QuestionTopic.SONSTIGE);

    constructor(topic: QuestionTopic) : this(topic.emoji, topic.labelRes, topic)

    fun matches(question: Question): Boolean = when (this) {
        ALL -> true
        IMAGES -> question.hasImage
        IMAGES_WITH_FILE -> question.imagePath != null
        else -> topic != null && Topics.of(question) == topic
    }

    companion object {
        fun all(): List<QuestionFilter> = values().toList()
    }
}
