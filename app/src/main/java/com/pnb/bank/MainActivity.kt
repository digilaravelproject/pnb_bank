package com.pnb.bank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.pnb.bank.databinding.ActivityMainBinding
import com.pnb.bank.ui.cardreissuance.CardReissuanceActivity
import com.pnb.bank.ui.otherservices.OtherServicesActivity
import com.pnb.bank.utils.AppConstants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Parent app se Intent mein aane wali key
        val serviceKey = intent.getStringExtra(AppConstants.KEY_SERVICE)
            ?: AppConstants.TEST_SERVICE_KEY   // Testing ke liye fallback

        Log.d("PNB_KIOSK", "Received service key: $serviceKey")

        // Key ke hisaab se screen open karo
        routeToScreen(serviceKey)
    }

    private fun routeToScreen(serviceKey: String) {
        val intent = when (serviceKey) {
            AppConstants.SERVICE_CARD_REISSUANCE -> {
                Intent(this, CardReissuanceActivity::class.java)
            }
            AppConstants.SERVICE_OTHER_SERVICES -> {
                Intent(this, OtherServicesActivity::class.java)
            }
            else -> {
                // Unknown key - default to Other Services
                Log.w("PNB_KIOSK", "Unknown service key: $serviceKey, defaulting to Other Services")
                Intent(this, OtherServicesActivity::class.java)
            }
        }
        startActivity(intent)
        finish() // MainActivity ko back stack se hata do
    }
}
