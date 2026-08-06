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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Parse Bearer Token if passed dynamically from parent app
        val token = intent.getStringExtra(AppConstants.KEY_AUTH_TOKEN)
        if (!token.isNullOrEmpty()) {
            ApiConstants.activeBearerToken = token
            AppLogger.i("Dynamic Auth Token received from parent app")
        } else {
            AppLogger.i("Using testing default Bearer Token: ${AppConstants.DEFAULT_BEARER_TOKEN}")
        }

        // Parse Request ID if passed dynamically from parent app
        val reqId = intent.getStringExtra(AppConstants.KEY_REQUEST_ID)
        if (!reqId.isNullOrEmpty()) {
            ApiConstants.activeRequestId = reqId
            AppLogger.i("Dynamic Request ID received from parent app: $reqId")
        } else {
            ApiConstants.activeRequestId = AppConstants.DEFAULT_REQUEST_ID
            AppLogger.i("Using testing default Request ID: ${AppConstants.DEFAULT_REQUEST_ID}")
        }

        // Parse Card Number if passed dynamically from parent app
        val cardNum = intent.getStringExtra(AppConstants.KEY_CARD_NUMBER)
        if (!cardNum.isNullOrEmpty()) {
            ApiConstants.activeCardNumber = cardNum
            AppLogger.i("Dynamic Card Number received from parent app: $cardNum")
        } else {
            ApiConstants.activeCardNumber = AppConstants.DEFAULT_CARD_NUMBER
            AppLogger.i("Using testing default Card Number: ${AppConstants.DEFAULT_CARD_NUMBER}")
        }

        // Parent app se Intent mein aane wali key
        val serviceKey = intent.getStringExtra(AppConstants.KEY_SERVICE)
            ?: AppConstants.TEST_SERVICE_KEY   // Testing ke liye fallback

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
