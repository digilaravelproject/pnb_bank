package com.pnb.bank.ui.creditscore

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.databinding.ActivityCreditScoreBinding
import com.pnb.bank.utils.hideSystemUI

class CreditScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityCreditScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Premium shadows for Android P and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = Color.parseColor("#50000000")
        }

        setupListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun setupListeners() {
        // Back navigation
        binding.btnBackContainer.setOnClickListener {
            finish()
        }

        // Home navigation
        binding.btnHomeContainer.setOnClickListener {
            finishAffinity()
        }

        // Submit form validation & execution
        binding.btnSubmitCreditScore.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etFullName.text.toString().trim()
        val mobile = binding.etMobileNumber.text.toString().trim()
        val documentId = binding.etDocumentId.text.toString().trim()
        val dob = binding.etDateOfBirth.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || documentId.isEmpty() || dob.isEmpty() || address.isEmpty() || pincode.isEmpty()) {
            showError("Please fill all the details to proceed.")
            return
        }

        if (mobile.length != 10) {
            showError("Please enter a valid 10-digit mobile number.")
            return
        }

        if (documentId.length != 10) {
            showError("Please enter a valid 10-character PAN number.")
            return
        }

        if (pincode.length != 6) {
            showError("Please enter a valid 6-digit pincode.")
            return
        }

        // Clear error if any
        binding.tvFormError.visibility = View.GONE

        // Successful validation response
        val msg = "Details submitted successfully!\nYour Credit Score will be processed shortly."
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showError(errorMsg: String) {
        binding.tvFormError.text = errorMsg
        binding.tvFormError.visibility = View.VISIBLE
    }
}
