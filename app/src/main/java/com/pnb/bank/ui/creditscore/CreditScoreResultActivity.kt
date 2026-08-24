package com.pnb.bank.ui.creditscore

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.pnb.bank.MainActivity
import com.pnb.bank.data.api.bankgateway.models.CreditScoreResponse
import com.pnb.bank.databinding.ActivityCreditScoreResultBinding
import com.pnb.bank.utils.hideSystemUI

class CreditScoreResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditScoreResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityCreditScoreResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Premium shadows for Android P and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = Color.parseColor("#50000000")
        }

        populateData()

        binding.btnResultDone.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun populateData() {
        val responseJson = intent.getStringExtra("credit_response_json")
        if (responseJson.isNullOrEmpty()) {
            finish()
            return
        }

        try {
            val response = Gson().fromJson(responseJson, CreditScoreResponse::class.java)
            val cirReport = response.data?.ccrResponse?.cirReportDataList?.firstOrNull()?.cirReportData
            val scoreDetail = cirReport?.scoreDetails?.firstOrNull()
            val scoreVal = scoreDetail?.value ?: "N/A"
            val scoreInt = scoreVal.toIntOrNull() ?: 0

            // 1. Render Score Info
            val ratingText = when {
                scoreInt >= 750 -> "Excellent Rating"
                scoreInt >= 700 -> "Good Rating"
                scoreInt >= 650 -> "Average Rating"
                else -> "Fair/Poor Rating"
            }
            val ratingColor = when {
                scoreInt >= 750 -> "#2E7D32" // Green
                scoreInt >= 700 -> "#1565C0" // Blue
                scoreInt >= 650 -> "#EF6C00" // Orange
                else -> "#C62828" // Red
            }

            binding.tvResultRating.text = ratingText
            binding.tvResultRating.setTextColor(Color.parseColor(ratingColor))
            binding.tvResultScore.setTextColor(Color.parseColor(ratingColor))
            
            // Sync gauge sweep animation
            binding.arcGaugeView.setScore(scoreInt)

            // Animate text count up from 300 to target score
            android.animation.ValueAnimator.ofInt(300, scoreInt).apply {
                duration = 1500
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { animation ->
                    binding.tvResultScore.text = (animation.animatedValue as Int).toString()
                }
                start()
            }

            val type = scoreDetail?.type ?: "N/A"
            val version = scoreDetail?.version ?: "N/A"
            binding.tvResultScoreDetails.text = "Type: $type | Version: $version"

            // 2. Render Personal Info
            val personalInfo = cirReport?.idAndContactInfo?.personalInfo
            val fullName = personalInfo?.name?.fullName ?: "N/A"
            val dob = personalInfo?.dateOfBirth ?: "N/A"
            val gender = personalInfo?.gender ?: "N/A"
            val ageStr = personalInfo?.age?.age ?: "N/A"

            binding.tvResultName.text = fullName
            binding.tvResultDob.text = dob
            binding.tvResultGender.text = gender
            binding.tvResultAge.text = if (ageStr != "N/A") "$ageStr Years" else "N/A"

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
}
