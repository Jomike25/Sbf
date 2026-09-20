package com.sbftrainer.app

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityAchievementsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.achievements_title)
    }

    override fun onResume() {
        super.onResume()
        rebuild()
    }

    private fun rebuild() {
        val unlockedCount = Achievements.unlockedCount()
        val total = Achievements.all.size
        binding.textAchievementsSummary.text =
            getString(R.string.achievements_header_format, unlockedCount, total)
        binding.progressAchievements.progress =
            if (total > 0) unlockedCount * 100 / total else 0

        binding.containerAchievements.removeAllViews()
        // Freigeschaltete zuerst, damit der Erfolg sichtbar oben steht
        val sorted = Achievements.all.sortedByDescending { Achievements.isUnlocked(it.id) }
        for (achievement in sorted) {
            binding.containerAchievements.addView(buildCard(achievement))
        }
    }

    private fun buildCard(achievement: Achievement): View {
        val unlocked = Achievements.isUnlocked(achievement.id)
        val (current, target) = achievement.progress(this)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setBackgroundResource(
            if (unlocked) R.drawable.bg_badge_unlocked else R.drawable.bg_badge_locked
        )
        val pad = dp(14)
        card.setPadding(pad, pad, pad, pad)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = dp(12)
        card.layoutParams = params

        val emoji = TextView(this)
        emoji.text = if (unlocked) achievement.emoji else "🔒"
        emoji.textSize = 26f
        if (!unlocked) emoji.alpha = 0.6f
        card.addView(emoji)

        val texts = LinearLayout(this)
        texts.orientation = LinearLayout.VERTICAL
        texts.setPadding(dp(14), 0, 0, 0)
        texts.layoutParams = LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        )

        val title = TextView(this)
        title.text = getString(achievement.titleRes)
        title.textSize = 15f
        title.setTypeface(title.typeface, Typeface.BOLD)
        title.setTextColor(getColor(if (unlocked) R.color.text_primary else R.color.locked_grey))
        texts.addView(title)

        val desc = TextView(this)
        desc.text = getString(achievement.descRes)
        desc.textSize = 13f
        desc.setTextColor(getColor(if (unlocked) R.color.text_secondary else R.color.locked_grey))
        texts.addView(desc)

        if (unlocked) {
            val at = GamificationStore.unlockedAt(achievement.id)
            if (at != null && at > 0L) {
                val date = TextView(this)
                date.text = getString(
                    R.string.achievements_unlocked_at_format, dateFormat.format(Date(at))
                )
                date.textSize = 12f
                date.setTextColor(getColor(R.color.amber_700))
                date.setPadding(0, dp(6), 0, 0)
                texts.addView(date)
            }
        } else {
            val progressText = TextView(this)
            progressText.text = getString(
                R.string.achievements_progress_format, current.coerceAtMost(target), target
            )
            progressText.textSize = 12f
            progressText.setTextColor(getColor(R.color.text_secondary))
            progressText.setPadding(0, dp(8), 0, dp(4))
            texts.addView(progressText)

            val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            bar.max = 100
            bar.progress = if (target > 0) (current * 100 / target).coerceIn(0, 100) else 0
            bar.progressDrawable = getDrawable(R.drawable.progress_category)
            bar.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(6)
            )
            texts.addView(bar)
        }

        card.addView(texts)
        return card
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
