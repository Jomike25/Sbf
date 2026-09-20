package com.sbftrainer.app

import android.content.Context

/**
 * Nutzereinstellungen, die zwischen App-Starts erhalten bleiben.
 */
object SettingsStore {
    private const val PREFS_NAME = "sbf_settings"
    private const val KEY_STALE_DAYS = "stale_days"

    /** Auswahlmoeglichkeiten fuer "Lange nicht geuebt" - muss zu R.array.stale_day_options passen. */
    val STALE_DAY_OPTIONS = listOf(1, 2, 3, 5, 7, 14, 30)

    const val DEFAULT_STALE_DAYS = 3

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getStaleDays(context: Context): Int =
        prefs(context).getInt(KEY_STALE_DAYS, DEFAULT_STALE_DAYS)

    fun setStaleDays(context: Context, days: Int) {
        prefs(context).edit().putInt(KEY_STALE_DAYS, days).apply()
    }

    fun getStaleThresholdMillis(context: Context): Long =
        getStaleDays(context) * 24L * 60 * 60 * 1000

    /** Index der aktuell gespeicherten Einstellung in STALE_DAY_OPTIONS. */
    fun staleDaysIndex(context: Context): Int {
        val current = getStaleDays(context)
        val index = STALE_DAY_OPTIONS.indexOf(current)
        return if (index >= 0) index else STALE_DAY_OPTIONS.indexOf(DEFAULT_STALE_DAYS)
    }
}
