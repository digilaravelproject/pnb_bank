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
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

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

        val userName = intent.getStringExtra("user_name") ?: ""
        val userMobile = intent.getStringExtra("user_mobile") ?: ""
        val userPan = intent.getStringExtra("user_pan") ?: ""
        val userEmail = intent.getStringExtra("user_email") ?: ""

        val apiRepository = com.pnb.bank.data.api.ApiRepository()

        binding.btnSendReport.setOnClickListener {
            // Show loading Toast or change UI
            android.widget.Toast.makeText(this, "Sending report...", android.widget.Toast.LENGTH_SHORT).show()
            binding.btnSendReport.isEnabled = false

            lifecycleScope.launch {
                val result = apiRepository.sendReportPdf(
                    name = userName,
                    mobileNumber = userMobile,
                    panNumber = userPan,
                    email = userEmail
                )
                
                binding.btnSendReport.isEnabled = true
                
                when (result) {
                    is com.pnb.bank.data.api.NetworkResult.Success -> {
                        val msg = result.data.message ?: "Email Send Successfully"
                        val intent = Intent(this@CreditScoreResultActivity, ComingSoonActivity::class.java).apply {
                            putExtra("feature_title", "Report Status")
                            putExtra("feature_message", msg)
                        }
                        startActivity(intent)
                    }
                    is com.pnb.bank.data.api.NetworkResult.Error -> {
                        android.widget.Toast.makeText(this@CreditScoreResultActivity, result.message ?: "Failed to send report", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

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
            val data = response.data
            val cirReport = data?.ccrResponse?.cirReportDataList?.firstOrNull()?.cirReportData
            val scoreDetail = cirReport?.scoreDetails?.firstOrNull()
            
            // Score resolution (V3 direct creditScore or FCIREXScore, or legacy scoreDetails)
            val scoreInt = data?.creditScore 
                ?: data?.creditReport?.score?.fcirexScore
                ?: scoreDetail?.value?.toIntOrNull() 
                ?: 0

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

            val type = scoreDetail?.type ?: "ERS"
            val version = data?.creditReport?.creditProfileHeader?.version ?: scoreDetail?.version ?: "3.1"
            binding.tvResultScoreDetails.text = "Type: $type | Version: $version"

            // 2. Render Personal Info
            val personalInfo = cirReport?.idAndContactInfo?.personalInfo
            val applicantDetails = data?.creditReport?.currentApplication?.currentApplicationDetails?.currentApplicantDetails
            
            val fullName = data?.name?.takeIf { it.isNotBlank() }
                ?: personalInfo?.name?.fullName
                ?: listOfNotNull(applicantDetails?.firstName, applicantDetails?.middleName1, applicantDetails?.lastName).joinToString(" ").takeIf { it.isNotBlank() }
                ?: "N/A"

            val rawDob = applicantDetails?.dateOfBirth ?: personalInfo?.dateOfBirth ?: "N/A"
            val dob = if (rawDob.length == 8 && rawDob.all { it.isDigit() }) {
                // Format YYYYMMDD to YYYY-MM-DD
                "${rawDob.substring(0, 4)}-${rawDob.substring(4, 6)}-${rawDob.substring(6, 8)}"
            } else {
                rawDob
            }

            val gender = when (applicantDetails?.genderCode ?: personalInfo?.gender) {
                "1", "M", "Male" -> "Male"
                "2", "F", "Female" -> "Female"
                else -> personalInfo?.gender ?: "N/A"
            }

            // Calculate age from DOB if age object is not present
            val calculatedAge = if (dob.length >= 4 && dob.substring(0, 4).all { it.isDigit() }) {
                val birthYear = dob.substring(0, 4).toIntOrNull()
                if (birthYear != null && birthYear > 1900) {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    "${currentYear - birthYear} Years"
                } else null
            } else null

            val ageStr = personalInfo?.age?.age?.let { "$it Years" } ?: calculatedAge ?: "N/A"

            binding.tvResultName.text = fullName
            binding.tvResultDob.text = dob
            binding.tvResultGender.text = gender
            binding.tvResultAge.text = ageStr

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
}
