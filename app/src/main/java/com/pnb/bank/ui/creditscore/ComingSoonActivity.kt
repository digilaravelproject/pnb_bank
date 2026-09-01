package com.pnb.bank.ui.creditscore

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.databinding.ActivityComingSoonBinding
import com.pnb.bank.utils.hideSystemUI

class ComingSoonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComingSoonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityComingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = Color.parseColor("#50000000")
        }

        val title = intent.getStringExtra("feature_title")
        val message = intent.getStringExtra("feature_message")

        if (!title.isNullOrEmpty()) {
            binding.tvComingSoonTitle.text = title
        }
        if (!message.isNullOrEmpty()) {
            binding.tvComingSoonSubtitle.text = message
        }

        binding.btnComingSoonBack.setOnClickListener {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}
