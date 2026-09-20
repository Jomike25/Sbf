package com.sbftrainer.app

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.result_title)

        val total = SessionState.questions.size
        val correct = SessionState.correctCount
        val percent = if (total > 0) correct * 100 / total else 0

        settleRound(total, correct)

        binding.textScore.text = getString(R.string.result_score_format, correct, total)
        animatePercent(percent)
        binding.textPraise.text = getString(praiseRes(percent, total, correct))

        showExamVerdict(total, correct, percent)
        showXpSummary()
        showLevelCard()
        showNewAchievements()
        showMissed()

        binding.buttonRetryMissed.setOnClickListener { retryMissed() }
        binding.buttonRestart.setOnClickListener { finish() }
        binding.buttonHome.setOnClickListener { goHome() }
    }

    /** Rundenbonus und Abzeichen genau einmal pro Runde verbuchen. */
    private fun settleRound(total: Int, correct: Int) {
        if (SessionState.settled) return
        SessionState.bonusXp = GamificationStore.recordRoundFinished(
            total, correct, SessionState.mode, SessionState.category, SessionState.bogenNumber
        )
        SessionState.newAchievements = Achievements.checkForNew(this)
        SessionState.settled = true
    }

    private fun animatePercent(percent: Int) {
        val animator = ValueAnimator.ofInt(0, percent)
        animator.duration = 900
        animator.addUpdateListener {
            binding.textScorePercent.text =
                getString(R.string.result_percent_format, it.animatedValue as Int)
        }
        animator.start()
    }

    private fun praiseRes(percent: Int, total: Int, correct: Int): Int = when {
        total > 0 && correct == total -> R.string.result_praise_perfect
        percent >= 90 -> R.string.result_praise_great
        percent >= 75 -> R.string.result_praise_good
        percent >= 50 -> R.string.result_praise_ok
        else -> R.string.result_praise_low
    }

    private fun showExamVerdict(total: Int, correct: Int, percent: Int) {
        if (SessionState.mode != QuizMode.EXAM || total == 0) return
        val goal = GamificationStore.EXAM_GOAL_PERCENT
        val passed = percent >= goal
        binding.textExamVerdict.text = getString(
            if (passed) R.string.result_exam_passed_format else R.string.result_exam_failed_format,
            goal
        )
        binding.textExamVerdict.setBackgroundResource(
            if (passed) R.drawable.bg_feedback_correct else R.drawable.bg_feedback_wrong
        )
        binding.textExamVerdict.setTextColor(
            getColor(if (passed) R.color.green_correct else R.color.red_wrong)
        )
        binding.textExamVerdict.visibility = View.VISIBLE
        binding.textExamDisclaimer.visibility = View.VISIBLE
    }

    private fun showXpSummary() {
        binding.textXpEarned.text = getString(R.string.result_xp_format, SessionState.totalXp())
        binding.textXpDetail.text = getString(
            R.string.result_xp_detail_format, SessionState.xpFromAnswers, SessionState.bonusXp
        )
        if (SessionState.bestCombo >= 2) {
            binding.textComboBest.text =
                getString(R.string.result_combo_format, SessionState.bestCombo)
            binding.textComboBest.visibility = View.VISIBLE
        } else {
            binding.textComboBest.visibility = View.GONE
        }
        val duration = SessionState.durationMillis
        if (duration > 0) {
            binding.textDuration.text =
                getString(R.string.result_duration_format, formatDuration(duration))
            binding.textDuration.visibility = View.VISIBLE
        } else {
            binding.textDuration.visibility = View.GONE
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    private fun showLevelCard() {
        val xp = GamificationStore.xp
        val level = Levels.levelFor(xp)
        binding.textLevelProgress.text = getString(
            R.string.result_level_progress_format, level, Levels.xpIntoLevel(xp), Levels.xpForNextLevel(xp)
        )

        val animator = ValueAnimator.ofInt(0, Levels.progressPercent(xp))
        animator.duration = 900
        animator.addUpdateListener { binding.progressLevel.progress = it.animatedValue as Int }
        animator.start()

        if (level > SessionState.levelBefore) {
            binding.textLevelUp.text = getString(R.string.result_level_up_format, level)
            Feedback.slideIn(binding.textLevelUp)
            binding.textLevelUp.postDelayed({ Feedback.pop(binding.textLevelUp, 1.15f) }, 260)
        }

        binding.textGoalReached.visibility =
            if (GamificationStore.goalReachedToday()) View.VISIBLE else View.GONE
    }

    private fun showNewAchievements() {
        val fresh = SessionState.newAchievements
        if (fresh.isEmpty()) return

        binding.textAchievementsHeader.visibility = View.VISIBLE
        fresh.forEachIndexed { index, achievement ->
            val card = buildAchievementCard(achievement)
            binding.containerAchievements.addView(card)
            card.visibility = View.INVISIBLE
            card.postDelayed({ Feedback.slideIn(card) }, 200L + index * 160L)
        }
    }

    private fun buildAchievementCard(achievement: Achievement): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.setBackgroundResource(R.drawable.bg_badge_unlocked)
        card.gravity = android.view.Gravity.CENTER_VERTICAL
        val pad = dp(14)
        card.setPadding(pad, pad, pad, pad)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(10)
        card.layoutParams = params

        val emoji = TextView(this)
        emoji.text = achievement.emoji
        emoji.textSize = 26f
        card.addView(emoji)

        val texts = LinearLayout(this)
        texts.orientation = LinearLayout.VERTICAL
        texts.setPadding(dp(14), 0, 0, 0)

        val title = TextView(this)
        title.text = getString(achievement.titleRes)
        title.textSize = 15f
        title.setTypeface(title.typeface, Typeface.BOLD)
        title.setTextColor(getColor(R.color.text_primary))
        texts.addView(title)

        val desc = TextView(this)
        desc.text = getString(achievement.descRes)
        desc.textSize = 13f
        desc.setTextColor(getColor(R.color.text_secondary))
        texts.addView(desc)

        card.addView(texts)
        return card
    }

    private fun showMissed() {
        if (SessionState.missed.isEmpty()) {
            binding.textMissedHeader.text = getString(R.string.result_no_missed)
            binding.buttonRetryMissed.visibility = View.GONE
            return
        }

        binding.textMissedHeader.text = getString(R.string.result_missed_header)
        for (q in SessionState.missed) {
            val row = TextView(this)
            row.text = "• ${q.question}\n   ✅ ${q.options[q.correctIndex]}"
            row.textSize = 14f
            row.setTextColor(getColor(R.color.text_primary))
            row.setPadding(0, 0, 0, dp(16))
            binding.containerMissed.addView(row)
        }

        binding.buttonRetryMissed.text =
            getString(R.string.result_retry_missed_format, SessionState.missed.size)
        binding.buttonRetryMissed.visibility = View.VISIBLE
    }

    private fun retryMissed() {
        if (!SessionState.startRetryOfMissed()) return
        startActivity(Intent(this, QuizActivity::class.java))
        finish()
    }

    private fun goHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            goHome()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
