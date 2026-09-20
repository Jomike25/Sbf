package com.sbftrainer.app

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityStatsBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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
        GamificationStore.refreshStreak()
        rebuildOverview()
        rebuildWeek()
        rebuildStats()
    }

    private fun rebuildOverview() {
        val xp = GamificationStore.xp
        val level = Levels.levelFor(xp)
        binding.textLevelBadge.text = level.toString()
        binding.textLevel.text = getString(R.string.hero_level_format, level)
        binding.textXpTotal.text = getString(R.string.hero_xp_total_format, xp)
        binding.textStreakChip.text = getString(R.string.quiz_combo_format, GamificationStore.streakDays)

        val animator = ValueAnimator.ofInt(0, Levels.progressPercent(xp))
        animator.duration = 650
        animator.addUpdateListener { binding.progressXp.progress = it.animatedValue as Int }
        animator.start()

        val answered = GamificationStore.totalAnswered
        val accuracy = if (answered > 0) GamificationStore.totalCorrect * 100 / answered else 0
        binding.textOverview.text = listOf(
            getString(R.string.stats_total_answered_format, answered),
            getString(R.string.stats_overall_accuracy_format, accuracy),
            getString(R.string.stats_best_combo_format, GamificationStore.bestCombo),
            getString(R.string.stats_longest_streak_format, GamificationStore.longestStreak),
            getString(R.string.stats_perfect_rounds_format, GamificationStore.perfectRounds)
        ).joinToString("\n")
    }

    /** Kleines Balkendiagramm der letzten 7 Tage, ganz ohne Chart-Bibliothek. */
    private fun rebuildWeek() {
        binding.containerWeek.removeAllViews()
        val activity = GamificationStore.recentActivity(7)
        val maxCount = maxOf(activity.maxOfOrNull { it.second } ?: 0, 1)
        val goal = GamificationStore.dailyGoal
        val barMaxHeight = dp(88)

        binding.textWeekEmpty.visibility =
            if (activity.all { it.second == 0 }) View.VISIBLE else View.GONE

        for ((day, count) in activity) {
            val column = LinearLayout(this)
            column.orientation = LinearLayout.VERTICAL
            column.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            column.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f
            )

            val countView = TextView(this)
            countView.text = if (count > 0) count.toString() else "–"
            countView.textSize = 11f
            countView.gravity = Gravity.CENTER
            countView.setTextColor(
                getColor(if (count > 0) R.color.text_primary else R.color.text_secondary)
            )
            column.addView(countView)

            val bar = View(this)
            val height = if (count == 0) dp(4) else {
                maxOf(dp(6), barMaxHeight * count / maxCount)
            }
            val barParams = LinearLayout.LayoutParams(dp(18), height)
            barParams.topMargin = dp(4)
            bar.layoutParams = barParams
            bar.setBackgroundResource(
                when {
                    count == 0 -> R.drawable.bg_bar_day_empty
                    count >= goal -> R.drawable.bg_bar_day_goal
                    else -> R.drawable.bg_bar_day
                }
            )
            column.addView(bar)

            val label = TextView(this)
            label.text = LocalDate.ofEpochDay(day)
                .dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.GERMANY)
            label.textSize = 11f
            label.gravity = Gravity.CENTER
            label.setTextColor(getColor(R.color.text_secondary))
            label.setPadding(0, dp(6), 0, 0)
            column.addView(label)

            binding.containerWeek.addView(column)
        }
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

        val coverage = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        coverage.max = 100
        coverage.progressDrawable = getDrawable(R.drawable.progress_category)
        coverage.progress = if (all.isNotEmpty()) answered * 100 / all.size else 0
        val coverageParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(8)
        )
        coverageParams.topMargin = dp(8)
        coverage.layoutParams = coverageParams
        card.addView(coverage)

        val markedView = TextView(this)
        markedView.text = getString(R.string.stats_marked_format, marked)
        markedView.setTextColor(getColor(R.color.text_secondary))
        markedView.setPadding(0, dp(10), 0, 0)
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
                GamificationStore.resetAll()
                rebuildOverview()
                rebuildWeek()
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
