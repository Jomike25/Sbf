package com.sbftrainer.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sbftrainer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardBinnen.setOnClickListener { openMode(QuestionRepository.CATEGORY_BINNEN) }
        binding.cardSee.setOnClickListener { openMode(QuestionRepository.CATEGORY_SEE) }
        binding.buttonStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val binnenCount = QuestionRepository.getAll(this, QuestionRepository.CATEGORY_BINNEN).size
        val seeCount = QuestionRepository.getAll(this, QuestionRepository.CATEGORY_SEE).size
        binding.textBinnenCount.text = getString(R.string.questions_count_format, binnenCount)
        binding.textSeeCount.text = getString(R.string.questions_count_format, seeCount)
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
