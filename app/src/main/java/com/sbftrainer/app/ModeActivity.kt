package com.sbftrainer.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityModeBinding

class ModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeBinding
    private lateinit var category: String
    private var selectedMode: QuizMode = QuizMode.ALL

    private val countValues = listOf(10, 20, 50, QuizSelector.COUNT_ALL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra(MainActivity.EXTRA_CATEGORY) ?: QuestionRepository.CATEGORY_BINNEN

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.mode_title_format, QuestionRepository.categoryLabel(this, category))

        val adapter = ArrayAdapter.createFromResource(
            this, R.array.count_options, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCount.adapter = adapter

        binding.cardModeAll.setOnClickListener { selectMode(QuizMode.ALL) }
        binding.cardModeMarked.setOnClickListener { selectMode(QuizMode.MARKED) }
        binding.cardModeWrong.setOnClickListener { selectMode(QuizMode.WRONG) }
        binding.cardModeStale.setOnClickListener { selectMode(QuizMode.STALE) }

        binding.buttonStart.setOnClickListener { startTraining() }

        selectMode(QuizMode.ALL)
    }

    override fun onResume() {
        super.onResume()
        refreshCounts()
    }

    private fun refreshCounts() {
        binding.textCountAll.text = getString(
            R.string.mode_available_format, QuizSelector.availableCount(this, category, QuizMode.ALL)
        )
        binding.textCountMarked.text = getString(
            R.string.mode_available_format, QuizSelector.availableCount(this, category, QuizMode.MARKED)
        )
        binding.textCountWrong.text = getString(
            R.string.mode_available_format, QuizSelector.availableCount(this, category, QuizMode.WRONG)
        )
        binding.textCountStale.text = getString(
            R.string.mode_available_format, QuizSelector.availableCount(this, category, QuizMode.STALE)
        )
    }

    private fun selectMode(mode: QuizMode) {
        selectedMode = mode
        binding.cardModeAll.setBackgroundResource(
            if (mode == QuizMode.ALL) R.drawable.bg_option_selected else R.drawable.bg_option_default
        )
        binding.cardModeMarked.setBackgroundResource(
            if (mode == QuizMode.MARKED) R.drawable.bg_option_selected else R.drawable.bg_option_default
        )
        binding.cardModeWrong.setBackgroundResource(
            if (mode == QuizMode.WRONG) R.drawable.bg_option_selected else R.drawable.bg_option_default
        )
        binding.cardModeStale.setBackgroundResource(
            if (mode == QuizMode.STALE) R.drawable.bg_option_selected else R.drawable.bg_option_default
        )
    }

    private fun startTraining() {
        val available = QuizSelector.availableCount(this, category, selectedMode)
        if (available == 0) {
            Toast.makeText(this, R.string.mode_none_available, Toast.LENGTH_LONG).show()
            return
        }
        val requestedCount = countValues[binding.spinnerCount.selectedItemPosition]
        val questions = QuizSelector.buildSession(this, category, selectedMode, requestedCount)
        SessionState.start(category, selectedMode, questions)

        startActivity(Intent(this, QuizActivity::class.java))
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
