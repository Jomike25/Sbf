package com.sbftrainer.app

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var optionViews: List<TextView>

    private var currentIndex = 0
    private var answered = false
    private var currentOrder: List<Int> = listOf(0, 1, 2, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionState.questions.isEmpty()) {
            finish()
            return
        }

        optionViews = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val categoryLabel = QuestionRepository.categoryLabel(this, SessionState.category)
        val filter = SessionState.filter
        title = when {
            SessionState.bogenNumber != null ->
                getString(R.string.mode_bogen_option_format, SessionState.bogenNumber) +
                    " – " + categoryLabel
            filter != QuestionFilter.ALL ->
                categoryLabel + " · " + filter.emoji + " " + getString(filter.labelRes)
            else -> categoryLabel
        }

        binding.optionA.setOnClickListener { selectOption(0) }
        binding.optionB.setOnClickListener { selectOption(1) }
        binding.optionC.setOnClickListener { selectOption(2) }
        binding.optionD.setOnClickListener { selectOption(3) }

        binding.buttonMark.setOnClickListener { toggleMark() }
        binding.buttonSound.setOnClickListener { toggleSound() }
        binding.buttonNext.setOnClickListener { goNext() }

        updateSoundIcon()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmation()
                }
            }
        )

        showQuestion()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) Feedback.release()
    }

    private fun showQuestion() {
        answered = false
        currentOrder = (0..3).shuffled()

        val q = SessionState.questions[currentIndex]
        binding.textProgress.text = getString(
            R.string.quiz_progress_format, currentIndex + 1, SessionState.questions.size
        )
        animateQuizProgress(currentIndex)
        binding.textQuestion.text = q.question
        showQuestionImage(q)

        val letters = listOf("A", "B", "C", "D")
        for (slot in 0..3) {
            val originalIndex = currentOrder[slot]
            optionViews[slot].text = "${letters[slot]}) ${q.options[originalIndex]}"
            optionViews[slot].setBackgroundResource(R.drawable.bg_option_default)
            optionViews[slot].isEnabled = true
            optionViews[slot].translationX = 0f
        }

        updateMarkIcon(q)
        updateScoreAndCombo(animate = false)
        binding.textFeedback.visibility = View.GONE
        binding.scrollQuiz.scrollTo(0, 0)

        binding.buttonNext.isEnabled = false
        binding.buttonNext.text = if (currentIndex == SessionState.questions.size - 1) {
            getString(R.string.quiz_finish)
        } else {
            getString(R.string.quiz_next)
        }
    }

    /** Fortschrittsbalken weich auf die Zahl der erledigten Fragen hochzaehlen. */
    private fun animateQuizProgress(done: Int) {
        val total = SessionState.questions.size
        val target = if (total > 0) done * 100 / total else 0
        val animator = ValueAnimator.ofInt(binding.progressQuiz.progress, target)
        animator.duration = 260
        animator.addUpdateListener { binding.progressQuiz.progress = it.animatedValue as Int }
        animator.start()
    }

    private fun showQuestionImage(q: Question) {
        val path = q.imagePath
        if (path != null) {
            val bitmap = try {
                assets.open(path).use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) {
                null
            }
            if (bitmap != null) {
                binding.imageQuestion.setImageBitmap(bitmap)
                binding.imageQuestion.visibility = View.VISIBLE
                binding.textImageHint.visibility = View.GONE
                return
            }
        }
        binding.imageQuestion.visibility = View.GONE
        binding.textImageHint.visibility = if (q.hasImage) View.VISIBLE else View.GONE
    }

    private fun selectOption(slot: Int) {
        if (answered) return
        answered = true

        val q = SessionState.questions[currentIndex]
        val chosenOriginalIndex = currentOrder[slot]
        val correct = chosenOriginalIndex == q.correctIndex
        val correctSlot = currentOrder.indexOf(q.correctIndex)

        for (i in 0..3) {
            optionViews[i].isEnabled = false
            optionViews[i].setBackgroundResource(
                when {
                    i == correctSlot -> R.drawable.bg_option_correct
                    i == slot && !correct -> R.drawable.bg_option_wrong
                    else -> R.drawable.bg_option_default
                }
            )
        }

        ProgressStore.recordAnswer(q.id, correct)

        if (correct) {
            SessionState.correctCount += 1
            SessionState.combo += 1
            if (SessionState.combo > SessionState.bestCombo) {
                SessionState.bestCombo = SessionState.combo
            }
        } else {
            SessionState.missed.add(q)
            SessionState.combo = 0
        }

        val earnedXp = GamificationStore.recordAnswer(correct, SessionState.combo)
        SessionState.xpFromAnswers += earnedXp

        Feedback.answer(binding.root, correct)
        if (correct) {
            Feedback.pop(optionViews[correctSlot], 1.04f)
        } else {
            Feedback.shake(optionViews[slot])
        }

        showFeedback(correct, earnedXp, correctSlot, q)
        updateScoreAndCombo(animate = true)
        animateQuizProgress(currentIndex + 1)

        binding.buttonNext.isEnabled = true
    }

    private fun showFeedback(correct: Boolean, earnedXp: Int, correctSlot: Int, q: Question) {
        val banner = binding.textFeedback
        if (correct) {
            val combo = SessionState.combo
            banner.text = when {
                combo >= 3 -> getString(R.string.quiz_feedback_combo_format, combo, earnedXp)
                earnedXp > 0 -> getString(R.string.quiz_feedback_correct_xp_format, earnedXp)
                else -> getString(R.string.quiz_feedback_correct)
            }
            banner.setBackgroundResource(R.drawable.bg_feedback_correct)
            banner.setTextColor(getColor(R.color.green_correct))
        } else {
            val letter = listOf("A", "B", "C", "D")[correctSlot]
            banner.text = getString(
                R.string.quiz_feedback_wrong_format, "$letter) ${q.options[q.correctIndex]}"
            )
            banner.setBackgroundResource(R.drawable.bg_feedback_wrong)
            banner.setTextColor(getColor(R.color.red_wrong))
        }
        Feedback.slideIn(banner)
    }

    private fun updateScoreAndCombo(animate: Boolean) {
        binding.textScore.text = getString(R.string.quiz_score_format, SessionState.correctCount)
        val combo = SessionState.combo
        if (combo >= 2) {
            val wasVisible = binding.textCombo.visibility == View.VISIBLE
            binding.textCombo.text = getString(R.string.quiz_combo_format, combo)
            binding.textCombo.visibility = View.VISIBLE
            if (animate) {
                if (wasVisible) Feedback.pop(binding.textCombo, 1.25f) else Feedback.slideIn(binding.textCombo)
            }
        } else {
            binding.textCombo.visibility = View.INVISIBLE
        }
    }

    private fun goNext() {
        if (currentIndex < SessionState.questions.size - 1) {
            currentIndex += 1
            showQuestion()
        } else {
            SessionState.durationMillis = System.currentTimeMillis() - SessionState.startedAt
            startActivity(Intent(this, ResultActivity::class.java))
            finish()
        }
    }

    private fun toggleMark() {
        val q = SessionState.questions[currentIndex]
        val marked = ProgressStore.toggleMarked(q.id)
        binding.buttonMark.setImageResource(
            if (marked) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
        if (marked) Feedback.pop(binding.buttonMark, 1.3f)
    }

    private fun updateMarkIcon(q: Question) {
        val marked = ProgressStore.getStat(q.id).marked
        binding.buttonMark.setImageResource(
            if (marked) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
    }

    private fun toggleSound() {
        GamificationStore.setSoundEnabled(!GamificationStore.soundEnabled)
        updateSoundIcon()
        if (GamificationStore.soundEnabled) Feedback.answer(binding.root, true)
    }

    private fun updateSoundIcon() {
        val on = GamificationStore.soundEnabled
        binding.buttonSound.setImageResource(
            if (on) R.drawable.ic_volume_on else R.drawable.ic_volume_off
        )
        binding.buttonSound.contentDescription =
            getString(if (on) R.string.quiz_sound_on else R.string.quiz_sound_off)
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.quiz_exit_title)
            .setMessage(R.string.quiz_exit_message)
            .setPositiveButton(R.string.quiz_exit_confirm) { _, _ -> finish() }
            .setNegativeButton(R.string.quiz_exit_cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
