package com.sbftrainer.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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

        binding.textScorePercent.text = getString(R.string.result_percent_format, percent)
        binding.textScore.text = getString(R.string.result_score_format, correct, total)

        if (SessionState.missed.isEmpty()) {
            binding.textMissedHeader.text = getString(R.string.result_no_missed)
        } else {
            for (q in SessionState.missed) {
                val row = TextView(this)
                row.text = "• ${q.question}\n   ✅ ${q.options[q.correctIndex]}"
                row.textSize = 14f
                row.setTextColor(getColor(R.color.text_primary))
                row.setPadding(0, 0, 0, dp(16))
                binding.containerMissed.addView(row)
            }
        }

        binding.buttonRestart.setOnClickListener { finish() }
        binding.buttonHome.setOnClickListener { goHome() }
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
