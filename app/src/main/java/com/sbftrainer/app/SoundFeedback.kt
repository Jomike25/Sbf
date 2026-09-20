package com.sbftrainer.app

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Kurze Feedback-Toene ueber ToneGenerator, damit keine Audio-Assets gebuendelt werden muessen.
 * TONE_PROP_ACK/NACK klingen deutlich angenehmer als der Standard-Klick der Options-Views.
 */
object SoundFeedback {
    private var generator: ToneGenerator? = null

    private fun get(): ToneGenerator? {
        var tg = generator
        if (tg == null) {
            tg = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (e: RuntimeException) {
                null
            }
            generator = tg
        }
        return tg
    }

    fun playCorrect() {
        get()?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    }

    fun playWrong() {
        get()?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    fun release() {
        generator?.release()
        generator = null
    }
}
