package com.sbftrainer.app

import android.content.Context
import org.json.JSONArray

object QuestionRepository {
    const val CATEGORY_BINNEN = "binnen"
    const val CATEGORY_SEE = "see"

    private val cache = mutableMapOf<String, List<Question>>()

    fun getAll(context: Context, category: String): List<Question> {
        cache[category]?.let { return it }
        val fileName = when (category) {
            CATEGORY_BINNEN -> "questions_binnen.json"
            CATEGORY_SEE -> "questions_see.json"
            else -> throw IllegalArgumentException("Unbekannte Kategorie: $category")
        }
        val json = context.applicationContext.assets.open(fileName)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val array = JSONArray(json)
        val list = ArrayList<Question>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val optionsArray = obj.getJSONArray("options")
            val options = ArrayList<String>(optionsArray.length())
            for (j in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(j))
            }
            list.add(
                Question(
                    id = obj.getString("id"),
                    category = obj.getString("category"),
                    question = obj.getString("question"),
                    options = options,
                    correctIndex = obj.getInt("correctIndex"),
                    hasImage = obj.optBoolean("hasImage", false)
                )
            )
        }
        cache[category] = list
        return list
    }

    fun categoryLabel(context: Context, category: String): String = when (category) {
        CATEGORY_BINNEN -> context.getString(R.string.category_binnen)
        CATEGORY_SEE -> context.getString(R.string.category_see)
        else -> category
    }
}
