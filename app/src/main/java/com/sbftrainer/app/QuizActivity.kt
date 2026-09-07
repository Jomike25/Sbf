package com.sbftrainer.app

import android.content.Intent
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
        title = QuestionRepository.categoryLabel(this, SessionState.category)

        binding.optionA.setOnClickListener { selectOption(0) }
        binding.optionB.setOnClickListener { selectOption(1) }
        binding.optionC.setOnClickListener { selectOption(2) }
        binding.optionD.setOnClickListener { selectOption(3) }

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

    private fun showQuestion() {
        answered = false
        currentOrder = (0..3).shuffled()

        val q = SessionState.questions[currentIndex]
        binding.textProgress.text = getString(
            R.string.quiz_progress_format, currentIndex + 1, SessionState.questions.size
        )
        binding.textQuestion.text = q.question
        binding.textImageHint.visibility = if (q.hasImage) View.VISIBLE else View.GONE

        val letters = listOf("A", "B", "C", "D")
        for (slot in 0..3) {
            val originalIndex = currentOrder[slot]
            optionViews[slot].text = "${letters[slot]}) ${q.options[originalIndex]}"
            optionViews[slot].setBackgroundResource(R.drawable.bg_option_default)
            optionViews[slot].isEnabled = true
        }

        updateMarkIcon(q)

        binding.buttonNext.isEnabled = false
        binding.buttonNext.text = if (currentIndex == SessionState.questions.size - 1) {
            getString(R.string.quiz_finish)
        } else {
            getString(R.string.quiz_next)
        }
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
        } else {
            SessionState.missed.add(q)
        }

        binding.buttonNext.isEnabled = true
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
