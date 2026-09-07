package com.sbftrainer.app

import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityStatsBinding

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.stats_title)

        binding.buttonReset.setOnClickListener { confirmReset() }
    }

    override fun onResume() {
        super.onResume()
        rebuildStats()
    }

    private fun rebuildStats() {
        binding.containerCategories.removeAllViews()
        addCategoryCard(QuestionRepository.CATEGORY_BINNEN)
        addCategoryCard(QuestionRepository.CATEGORY_SEE)
    }

    private fun addCategoryCard(category: String) {
        val all = QuestionRepository.getAll(this, category)
        val answered = all.count { ProgressStore.getStat(it.id).timesShown > 0 }
        val marked = all.count { ProgressStore.getStat(it.id).marked }
        val totalShown = all.sumOf { ProgressStore.getStat(it.id).timesShown }
        val totalCorrect = all.sumOf { ProgressStore.getStat(it.id).timesCorrect }
        val accuracy = if (totalShown > 0) totalCorrect * 100 / totalShown else 0
        val weakest = all.filter { ProgressStore.getStat(it.id).timesWrong > 0 }
            .sortedByDescending { ProgressStore.getStat(it.id).timesWrong - ProgressStore.getStat(it.id).timesCorrect }
            .take(10)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.bg_card)
        val pad = dp(18)
        card.setPadding(pad, pad, pad, pad)
        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardParams.topMargin = dp(16)
        card.layoutParams = cardParams

        val titleView = TextView(this)
        titleView.text = QuestionRepository.categoryLabel(this, category)
        titleView.textSize = 18f
        titleView.setTypeface(titleView.typeface, Typeface.BOLD)
        titleView.setTextColor(getColor(R.color.navy_700))
        card.addView(titleView)

        val answeredView = TextView(this)
        answeredView.text = getString(R.string.stats_answered_format, answered, all.size)
        answeredView.setTextColor(getColor(R.color.text_secondary))
        answeredView.setPadding(0, dp(8), 0, 0)
        card.addView(answeredView)

        val markedView = TextView(this)
        markedView.text = getString(R.string.stats_marked_format, marked)
        markedView.setTextColor(getColor(R.color.text_secondary))
        markedView.setPadding(0, dp(4), 0, 0)
        card.addView(markedView)

        val accuracyView = TextView(this)
        accuracyView.text = getString(R.string.stats_accuracy_format, accuracy)
        accuracyView.setTextColor(getColor(R.color.text_secondary))
        accuracyView.setPadding(0, dp(4), 0, 0)
        card.addView(accuracyView)

        if (weakest.isEmpty()) {
            val noData = TextView(this)
            noData.text = getString(R.string.stats_no_data)
            noData.setTextColor(getColor(R.color.text_secondary))
            noData.setPadding(0, dp(16), 0, 0)
            card.addView(noData)
        } else {
            val weakestHeader = TextView(this)
            weakestHeader.text = getString(R.string.stats_weakest_header)
            weakestHeader.setTypeface(weakestHeader.typeface, Typeface.BOLD)
            weakestHeader.setTextColor(getColor(R.color.text_primary))
            weakestHeader.setPadding(0, dp(16), 0, dp(6))
            card.addView(weakestHeader)

            for (q in weakest) {
                val stat = ProgressStore.getStat(q.id)
                val row = TextView(this)
                row.text = "• ${q.question}  (${stat.timesWrong}x falsch)"
                row.textSize = 13f
                row.setTextColor(getColor(R.color.text_secondary))
                row.setPadding(0, dp(4), 0, 0)
                card.addView(row)
            }
        }

        binding.containerCategories.addView(card)
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.stats_reset_title)
            .setMessage(R.string.stats_reset_message)
            .setPositiveButton(R.string.stats_reset_confirm) { _, _ ->
                ProgressStore.resetAll()
                rebuildStats()
            }
            .setNegativeButton(R.string.stats_reset_cancel, null)
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
