package com.sbftrainer.app

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.CycleInterpolator
import android.animation.ObjectAnimator

/**
 * Kurze Rueckmeldungen auf eine Antwort: Ton, Vibration und kleine Animationen.
 * Alles optional - faellt still aus, wenn das Geraet nicht mitspielt.
 */
object Feedback {

    private var toneGenerator: ToneGenerator? = null

    private fun tones(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (e: Exception) {
                null
            }
        }
        return toneGenerator
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // egal - Ressource wird ohnehin verworfen
        }
        toneGenerator = null
    }

    fun answer(view: View, correct: Boolean) {
        haptic(view, correct)
        if (GamificationStore.soundEnabled) sound(correct)
    }

    private fun haptic(view: View, correct: Boolean) {
        try {
            val constant = when {
                correct -> HapticFeedbackConstants.CONTEXT_CLICK
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.REJECT
                else -> HapticFeedbackConstants.LONG_PRESS
            }
            view.performHapticFeedback(constant)
        } catch (e: Exception) {
            // Geraet ohne Haptik - nicht weiter wichtig
        }
    }

    private fun sound(correct: Boolean) {
        val generator = tones() ?: return
        try {
            if (correct) {
                // Ein zweitoniges Bestaetigungssignal statt eines einzelnen, harten Pieptons -
                // klingt freundlicher fuer eine richtige Antwort.
                generator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
            } else {
                generator.startTone(ToneGenerator.TONE_PROP_NACK, 220)
            }
        } catch (e: Exception) {
            // Ton fehlgeschlagen - stumm weitermachen
        }
    }

    /** Kurzes Aufpoppen, z. B. fuer die richtige Antwort oder eine neue Combo. */
    fun pop(view: View, scale: Float = 1.06f) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.animate()
            .scaleX(scale).scaleY(scale)
            .setDuration(110)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
            }
            .start()
    }

    /** Wackeln fuer eine falsche Antwort. */
    fun shake(view: View) {
        val amount = 12f * view.resources.displayMetrics.density
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, amount).apply {
            duration = 320
            interpolator = CycleInterpolator(2f)
            start()
        }
    }

    /** Einblenden von unten, fuer Feedback-Banner und Abzeichen-Karten. */
    fun slideIn(view: View) {
        view.alpha = 0f
        view.translationY = 24f * view.resources.displayMetrics.density
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .start()
    }
}
