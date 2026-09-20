package com.sbftrainer.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var optionViews: List<TextView>

    private var currentIndex = 0
    private var answered = false
    private var hintUsed = false
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
        title = SessionState.bogenNumber?.let {
            getString(R.string.mode_bogen_option_format, it) + " – " + categoryLabel
        } ?: categoryLabel

        binding.optionA.setOnClickListener { selectOption(0) }
        binding.optionB.setOnClickListener { selectOption(1) }
        binding.optionC.setOnClickListener { selectOption(2) }
        binding.optionD.setOnClickListener { selectOption(3) }

        for (option in optionViews) {
            option.isSoundEffectsEnabled = false
        }

        binding.buttonHint.text = getString(R.string.quiz_hint_button_format, ProgressStore.HINT_COST)
        binding.buttonHint.setOnClickListener { useHint() }

        binding.buttonMark.setOnClickListener { toggleMark() }
        binding.buttonNext.setOnClickListener { goNext() }

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
        if (isFinishing) {
            SoundFeedback.release()
        }
    }

    private fun showQuestion() {
        answered = false
        hintUsed = false
        currentOrder = (0..3).shuffled()

        val q = SessionState.questions[currentIndex]
        binding.textProgress.text = getString(
            R.string.quiz_progress_format, currentIndex + 1, SessionState.questions.size
        )
        binding.textQuestion.text = q.question
        showQuestionImage(q)
        updateCoinsDisplay()

        val letters = listOf("A", "B", "C", "D")
        for (slot in 0..3) {
            val originalIndex = currentOrder[slot]
            optionViews[slot].text = "${letters[slot]}) ${q.options[originalIndex]}"
            optionViews[slot].setBackgroundResource(R.drawable.bg_option_default)
            optionViews[slot].isEnabled = true
            optionViews[slot].alpha = 1f
        }

        binding.containerExplanation.visibility = View.GONE
        binding.textMnemonic.visibility = View.GONE
        binding.containerExplanationOptions.removeAllViews()

        binding.buttonHint.isEnabled = ProgressStore.getCoins() >= ProgressStore.HINT_COST
        binding.buttonHint.visibility = View.VISIBLE

        updateMarkIcon(q)

        binding.buttonNext.isEnabled = false
        binding.buttonNext.text = if (currentIndex == SessionState.questions.size - 1) {
            getString(R.string.quiz_finish)
        } else {
            getString(R.string.quiz_next)
        }
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

    private fun useHint() {
        if (answered || hintUsed) return
        if (!ProgressStore.spendCoins(ProgressStore.HINT_COST)) {
            Toast.makeText(this, R.string.quiz_hint_no_coins, Toast.LENGTH_SHORT).show()
            return
        }
        hintUsed = true
        updateCoinsDisplay()
        binding.buttonHint.isEnabled = false

        val q = SessionState.questions[currentIndex]
        val correctSlot = currentOrder.indexOf(q.correctIndex)
        val eliminable = (0..3).filter { it != correctSlot && optionViews[it].isEnabled }
        val slotToEliminate = eliminable.randomOrNull() ?: return
        optionViews[slotToEliminate].isEnabled = false
        optionViews[slotToEliminate].alpha = 0.35f
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
        binding.buttonHint.isEnabled = false

        val reward = ProgressStore.recordAnswer(q.id, correct)
        if (correct) {
            SessionState.correctCount += 1
            SoundFeedback.playCorrect()
            if (reward.streakBonus) {
                Toast.makeText(
                    this,
                    getString(R.string.quiz_streak_bonus_format, reward.streak, reward.coinsEarned),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            SessionState.missed.add(q)
            SoundFeedback.playWrong()
        }
        updateCoinsDisplay()

        showExplanation(q, correctSlot)

        binding.buttonNext.isEnabled = true
    }

    private fun showExplanation(q: Question, correctSlot: Int) {
        binding.containerExplanationOptions.removeAllViews()
        val letters = listOf("A", "B", "C", "D")

        for (slot in 0..3) {
            val originalIndex = currentOrder[slot]
            val isCorrect = slot == correctSlot
            val explanationText = q.optionExplanations.getOrNull(originalIndex)?.takeIf { it.isNotBlank() }
                ?: getString(
                    if (isCorrect) R.string.quiz_explanation_fallback_correct
                    else R.string.quiz_explanation_fallback_wrong
                )

            val row = TextView(this)
            val icon = if (isCorrect) "✅" else "❌"
            row.text = "$icon ${letters[slot]}) $explanationText"
            row.textSize = 13f
            row.setTextColor(
                getColor(if (isCorrect) R.color.green_correct else R.color.text_secondary)
            )
            row.setPadding(0, dp(6), 0, 0)
            binding.containerExplanationOptions.addView(row)
        }

        val mnemonic = q.mnemonic?.takeIf { it.isNotBlank() }
        if (mnemonic != null) {
            binding.textMnemonic.text = getString(R.string.quiz_mnemonic_format, mnemonic)
            binding.textMnemonic.visibility = View.VISIBLE
        } else {
            binding.textMnemonic.visibility = View.GONE
        }

        binding.containerExplanation.visibility = View.VISIBLE
    }

    private fun updateCoinsDisplay() {
        binding.textCoins.text = getString(R.string.quiz_coins_format, ProgressStore.getCoins())
    }

    private fun goNext() {
        if (currentIndex < SessionState.questions.size - 1) {
            currentIndex += 1
            showQuestion()
        } else {
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
    }

    private fun updateMarkIcon(q: Question) {
        val marked = ProgressStore.getStat(q.id).marked
        binding.buttonMark.setImageResource(
            if (marked) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.quiz_exit_title)
            .setMessage(R.string.quiz_exit_message)
            .setPositiveButton(R.string.quiz_exit_confirm) { _, _ -> finish() }
            .setNegativeButton(R.string.quiz_exit_cancel, null)
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
