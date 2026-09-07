package com.sbftrainer.app

import android.content.Context
import org.json.JSONObject

object BogenRepository {

    private val cache = mutableMapOf<String, Map<Int, List<String>>>()

    private fun fileName(category: String) = when (category) {
        QuestionRepository.CATEGORY_BINNEN -> "boegen_binnen.json"
        QuestionRepository.CATEGORY_SEE -> "boegen_see.json"
        else -> throw IllegalArgumentException("Unbekannte Kategorie: $category")
    }

    private fun getAll(context: Context, category: String): Map<Int, List<String>> {
        cache[category]?.let { return it }
        val json = context.applicationContext.assets.open(fileName(category))
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val obj = JSONObject(json)
        val result = LinkedHashMap<Int, List<String>>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val bogenKey = keys.next()
            val array = obj.getJSONArray(bogenKey)
            val ids = ArrayList<String>(array.length())
            for (i in 0 until array.length()) {
                ids.add(array.getString(i))
            }
            result[bogenKey.toInt()] = ids
        }
        cache[category] = result
        return result
    }

    fun bogenNumbers(context: Context, category: String): List<Int> =
        getAll(context, category).keys.sorted()

    fun questionIdsForBogen(context: Context, category: String, bogenNumber: Int): List<String> =
        getAll(context, category)[bogenNumber] ?: emptyList()
}
