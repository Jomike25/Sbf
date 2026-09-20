package com.sbftrainer.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.sbftrainer.app.databinding.ActivityModeBinding

class ModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeBinding
    private lateinit var category: String
    private var selectedMode: QuizMode = QuizMode.ALL
    private var selectedFilter: QuestionFilter = QuestionFilter.ALL
    private var bogenNumbers: List<Int> = emptyList()

    private val countValues = listOf(10, 20, 50, QuizSelector.COUNT_ALL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra(MainActivity.EXTRA_CATEGORY) ?: QuestionRepository.CATEGORY_BINNEN

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.mode_title_format, QuestionRepository.categoryLabel(this, category))

        val countAdapter = ArrayAdapter.createFromResource(
            this, R.array.count_options, android.R.layout.simple_spinner_item
        )
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCount.adapter = countAdapter

        val hasExamMode = category != QuestionRepository.CATEGORY_ALL
        if (hasExamMode) {
            bogenNumbers = BogenRepository.bogenNumbers(this, category)
            binding.textCountExam.text = getString(R.string.mode_bogen_count_format, bogenNumbers.size)
        } else {
            binding.cardModeExam.visibility = View.GONE
        }

        binding.cardModeAll.setOnClickListener { selectMode(QuizMode.ALL) }
        binding.cardModeSmart.setOnClickListener { selectMode(QuizMode.SMART) }
        binding.cardModeMarked.setOnClickListener { selectMode(QuizMode.MARKED) }
        binding.cardModeWrong.setOnClickListener { selectMode(QuizMode.WRONG) }
        binding.cardModeStale.setOnClickListener { selectMode(QuizMode.STALE) }
        binding.cardModeExam.setOnClickListener { selectMode(QuizMode.EXAM) }

        binding.buttonStart.setOnClickListener { startTraining() }

        buildTopicChips()
        selectMode(QuizMode.ALL, animate = false)
    }

    override fun onResume() {
        super.onResume()
        refreshCounts()
        refreshBogenSpinner()
    }

    /** Chips fuer die Unterkategorien; leere Kategorien werden weggelassen. */
    private fun buildTopicChips() {
        binding.chipGroupTopics.removeAllViews()
        for (filter in QuestionFilter.all()) {
            val count = QuizSelector.filterCount(this, category, filter)
            if (count == 0) continue
            val chip = layoutInflater.inflate(
                R.layout.item_topic_chip, binding.chipGroupTopics, false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = getString(
                R.string.filter_chip_format, filter.emoji, getString(filter.labelRes), count
            )
            chip.tag = filter
            chip.isChecked = filter == selectedFilter
            binding.chipGroupTopics.addView(chip)
        }
        binding.chipGroupTopics.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            selectedFilter = chip?.tag as? QuestionFilter ?: QuestionFilter.ALL
            refreshCounts()
        }
    }

    private fun refreshCounts() {
        binding.textCountAll.text = getString(
            R.string.mode_available_format,
            QuizSelector.availableCount(this, category, QuizMode.ALL, selectedFilter)
        )
        binding.textCountSmart.text = getString(
            R.string.mode_available_format,
            QuizSelector.availableCount(this, category, QuizMode.SMART, selectedFilter)
        )
        binding.textCountMarked.text = getString(
            R.string.mode_available_format,
            QuizSelector.availableCount(this, category, QuizMode.MARKED, selectedFilter)
        )
        binding.textCountWrong.text = getString(
            R.string.mode_available_format,
            QuizSelector.availableCount(this, category, QuizMode.WRONG, selectedFilter)
        )
        binding.textCountStale.text = getString(
            R.string.mode_available_format,
            QuizSelector.availableCount(this, category, QuizMode.STALE, selectedFilter)
        )
    }

    /** Zeigt im Bogen-Menue, welche Boegen schon geuebt wurden. */
    private fun refreshBogenSpinner() {
        if (bogenNumbers.isEmpty()) return
        val done = GamificationStore.examsCompletedCount(category)
        val labels = bogenNumbers.map { number ->
            if (GamificationStore.isExamCompleted(category, number)) {
                getString(R.string.mode_bogen_option_done_format, number)
            } else {
                getString(R.string.mode_bogen_option_format, number)
            }
        }
        val selected = binding.spinnerBogen.selectedItemPosition
        val bogenAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        bogenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBogen.adapter = bogenAdapter
        if (selected in labels.indices) binding.spinnerBogen.setSelection(selected)

        if (done > 0) {
            binding.textExamDone.text =
                getString(R.string.mode_exam_done_format, done, bogenNumbers.size)
            binding.textExamDone.visibility = View.VISIBLE
        } else {
            binding.textExamDone.visibility = View.GONE
        }
    }

    private fun selectMode(mode: QuizMode, animate: Boolean = true) {
        selectedMode = mode
        val cards = mapOf(
            QuizMode.ALL to binding.cardModeAll,
            QuizMode.SMART to binding.cardModeSmart,
            QuizMode.MARKED to binding.cardModeMarked,
            QuizMode.WRONG to binding.cardModeWrong,
            QuizMode.STALE to binding.cardModeStale,
            QuizMode.EXAM to binding.cardModeExam
        )
        for ((cardMode, card) in cards) {
            card.setBackgroundResource(
                if (cardMode == mode) R.drawable.bg_option_selected else R.drawable.bg_option_default
            )
        }
        if (animate) cards[mode]?.let { Feedback.pop(it, 1.02f) }

        val isExam = mode == QuizMode.EXAM
        binding.labelTopic.visibility = if (isExam) View.GONE else View.VISIBLE
        binding.chipGroupTopics.visibility = if (isExam) View.GONE else View.VISIBLE
        binding.textTopicHint.text = getString(
            if (isExam) R.string.mode_topic_exam_hint else R.string.mode_topic_hint
        )
        binding.labelCount.visibility = if (isExam) View.GONE else View.VISIBLE
        binding.spinnerCount.visibility = if (isExam) View.GONE else View.VISIBLE
        binding.labelBogen.visibility = if (isExam) View.VISIBLE else View.GONE
        binding.spinnerBogen.visibility = if (isExam) View.VISIBLE else View.GONE
    }

    private fun startTraining() {
        if (selectedMode == QuizMode.EXAM) {
            if (bogenNumbers.isEmpty()) {
                Toast.makeText(this, R.string.mode_none_available, Toast.LENGTH_LONG).show()
                return
            }
            val bogenNumber = bogenNumbers[binding.spinnerBogen.selectedItemPosition]
            val questions = QuizSelector.examSession(this, category, bogenNumber)
            SessionState.start(category, selectedMode, questions, bogenNumber)
            startActivity(Intent(this, QuizActivity::class.java))
            return
        }

        val available = QuizSelector.availableCount(this, category, selectedMode, selectedFilter)
        if (available == 0) {
            Toast.makeText(this, R.string.mode_none_available, Toast.LENGTH_LONG).show()
            return
        }
        val requestedCount = countValues[binding.spinnerCount.selectedItemPosition]
        val questions =
            QuizSelector.buildSession(this, category, selectedMode, requestedCount, selectedFilter)
        SessionState.start(category, selectedMode, questions, null, selectedFilter)

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
