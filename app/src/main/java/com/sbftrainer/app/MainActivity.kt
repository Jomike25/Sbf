package com.sbftrainer.app

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityMainBinding
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardBinnen.setOnClickListener { openMode(QuestionRepository.CATEGORY_BINNEN) }
        binding.cardSee.setOnClickListener { openMode(QuestionRepository.CATEGORY_SEE) }
        binding.cardCombined.setOnClickListener { openMode(QuestionRepository.CATEGORY_ALL) }
        binding.buttonStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        binding.buttonAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        binding.buttonQuickStart.setOnClickListener { startQuickSession() }
        binding.containerGoal.setOnClickListener { showGoalDialog() }
    }

    override fun onResume() {
        super.onResume()
        GamificationStore.refreshStreak()
        updateCategoryCards()
        updateHero()
    }

    private fun updateCategoryCards() {
        val binnen = QuestionRepository.getAll(this, QuestionRepository.CATEGORY_BINNEN)
        val see = QuestionRepository.getAll(this, QuestionRepository.CATEGORY_SEE)

        binding.textBinnenCount.text = getString(R.string.questions_count_format, binnen.size)
        binding.textSeeCount.text = getString(R.string.questions_count_format, see.size)
        binding.textCombinedCount.text =
            getString(R.string.questions_count_format, binnen.size + see.size)

        val binnenPercent = practisedPercent(binnen)
        val seePercent = practisedPercent(see)
        animateProgress(binding.progressBinnen, binnenPercent)
        animateProgress(binding.progressSee, seePercent)
        binding.textBinnenProgress.text = getString(R.string.category_progress_format, binnenPercent)
        binding.textSeeProgress.text = getString(R.string.category_progress_format, seePercent)
    }

    private fun practisedPercent(questions: List<Question>): Int {
        if (questions.isEmpty()) return 0
        val practised = questions.count { ProgressStore.getStat(it.id).timesShown > 0 }
        return practised * 100 / questions.size
    }

    private fun updateHero() {
        val xp = GamificationStore.xp
        val level = Levels.levelFor(xp)

        binding.textLevelBadge.text = level.toString()
        binding.textLevel.text = getString(R.string.hero_level_format, level)
        binding.textXp.text = getString(
            R.string.hero_xp_format,
            Levels.xpIntoLevel(xp),
            Levels.xpForNextLevel(xp),
            level + 1
        )
        animateProgress(binding.progressXp, Levels.progressPercent(xp))

        val streak = GamificationStore.streakDays
        binding.textStreak.text = if (streak > 0) {
            getString(R.string.hero_streak_format, streak)
        } else {
            getString(R.string.hero_streak_zero)
        }
        binding.textStreakChip.text = getString(R.string.quiz_combo_format, streak)

        val answeredToday = GamificationStore.answeredToday()
        val goal = GamificationStore.dailyGoal
        binding.textGoal.text = if (answeredToday >= goal) {
            getString(R.string.hero_goal_done_format, answeredToday)
        } else {
            getString(R.string.hero_goal_format, answeredToday, goal)
        }
        val goalPercent = if (goal > 0) (answeredToday * 100 / goal).coerceAtMost(100) else 0
        animateProgress(binding.progressGoal, goalPercent)

        binding.textGreeting.text = getString(greetingRes())
        binding.buttonAchievements.text = getString(
            R.string.achievements_button_format, Achievements.unlockedCount(), Achievements.all.size
        )
    }

    private fun greetingRes(): Int = when (LocalTime.now().hour) {
        in 0..10 -> R.string.hero_greeting_morning
        in 11..17 -> R.string.hero_greeting_day
        else -> R.string.hero_greeting_evening
    }

    private fun animateProgress(bar: ProgressBar, target: Int) {
        val animator = ValueAnimator.ofInt(0, target.coerceIn(0, 100))
        animator.duration = 650
        animator.addUpdateListener { bar.progress = it.animatedValue as Int }
        animator.start()
    }

    private fun startQuickSession() {
        val questions = QuizSelector.buildSession(
            this, QuestionRepository.CATEGORY_ALL, QuizMode.SMART, QuizSelector.QUICK_COUNT
        )
        if (questions.isEmpty()) {
            Toast.makeText(this, R.string.mode_none_available, Toast.LENGTH_LONG).show()
            return
        }
        Feedback.pop(binding.buttonQuickStart)
        SessionState.start(QuestionRepository.CATEGORY_ALL, QuizMode.SMART, questions)
        startActivity(Intent(this, QuizActivity::class.java))
    }

    private fun showGoalDialog() {
        val options = GamificationStore.GOAL_OPTIONS
        val labels = options.map { getString(R.string.goal_option_format, it) }.toTypedArray()
        val current = options.indexOf(GamificationStore.dailyGoal)
        AlertDialog.Builder(this)
            .setTitle(R.string.goal_dialog_title)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                GamificationStore.setDailyGoal(options[which])
                updateHero()
                dialog.dismiss()
            }
            .show()
    }

    private fun openMode(category: String) {
        val intent = Intent(this, ModeActivity::class.java)
        intent.putExtra(EXTRA_CATEGORY, category)
        startActivity(intent)
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }
}
