package com.pnb.bank

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.data.api.ApiConstants
import com.pnb.bank.databinding.ActivityMainBinding
import com.pnb.bank.ui.cardreissuance.CardReissuanceActivity
import com.pnb.bank.ui.otherservices.OtherServicesActivity
import com.pnb.bank.utils.AppConstants
import com.pnb.bank.utils.AppLogger
import com.pnb.bank.utils.hideSystemUI

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Parse Bearer Token passed dynamically from parent app
        val token = intent.getStringExtra(AppConstants.KEY_AUTH_TOKEN) ?: ""
        if (token.isNotEmpty()) {
            ApiConstants.activeBearerToken = token
            AppLogger.i("Dynamic Auth Token received from parent app: $token")
        } else {
            ApiConstants.activeBearerToken = AppConstants.DEFAULT_BEARER_TOKEN
            AppLogger.i("Using testing default Bearer Token: ${AppConstants.DEFAULT_BEARER_TOKEN}")
        }

        // Auto-generate Request ID as current timestamp
        AppLogger.i("Current timestamp Request ID generated: ${ApiConstants.activeRequestId}")

        // Parse Card Number passed dynamically from parent app
        val passedCardNumber = intent.getStringExtra(AppConstants.KEY_CARD_NUMBER) ?: ""
        if (passedCardNumber.isNotEmpty()) {
            ApiConstants.activeCardNumber = passedCardNumber
            AppLogger.i("Dynamic Card Number received from parent app: $passedCardNumber")
        } else {
            ApiConstants.activeCardNumber = AppConstants.DEFAULT_CARD_NUMBER
            AppLogger.i("Using fallback default Card Number: ${AppConstants.DEFAULT_CARD_NUMBER}")
        }

        // Service Key passed dynamically from parent app (with MODE 1 fallback)
        val intentServiceKey = intent.getStringExtra(AppConstants.KEY_SERVICE) ?: ""
        val serviceKey = if (intentServiceKey.isNotEmpty()) intentServiceKey else AppConstants.TEST_SERVICE_KEY

        AppLogger.d("Received service key: $serviceKey")

        routeToScreen(serviceKey)
    }

    private fun routeToScreen(serviceKey: String) {
        val targetIntent = when (serviceKey) {
            AppConstants.SERVICE_CARD_REISSUANCE -> {
                Intent(this, CardReissuanceActivity::class.java)
            }
            AppConstants.SERVICE_OTHER_SERVICES -> {
                Intent(this, OtherServicesActivity::class.java)
            }
            else -> {
                AppLogger.w("Unknown service key: $serviceKey, defaulting to Card Reissuance")
                Intent(this, CardReissuanceActivity::class.java)
            }
        }
        startActivity(targetIntent)
        finish()
    }
}
