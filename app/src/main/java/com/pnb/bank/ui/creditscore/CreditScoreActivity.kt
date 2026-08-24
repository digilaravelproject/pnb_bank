package com.pnb.bank.ui.creditscore

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.R
import com.pnb.bank.databinding.ActivityCreditScoreBinding
import com.pnb.bank.utils.hideSystemUI

class CreditScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditScoreBinding
    private var activeEditText: EditText? = null

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

        setupFocusListeners()
        setupKeyboard()
        setupListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun setupFocusListeners() {
        val editTexts = listOf(
            binding.etFullName,
            binding.etMobileNumber,
            binding.etDocumentId,
            binding.etDateOfBirth,
            binding.etAddress,
            binding.etPincode
        )

        editTexts.forEach { editText ->
            // Prevent the soft keyboard from popping up automatically
            editText.showSoftInputOnFocus = false

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeEditText = editText
                    binding.layoutKeyboard.visibility = View.VISIBLE
                }
            }

            editText.setOnClickListener {
                activeEditText = editText
                binding.layoutKeyboard.visibility = View.VISIBLE
            }
        }

        // Clear focus on start to keep keyboard hidden initially
        binding.etFullName.clearFocus()
        activeEditText = null
        binding.layoutKeyboard.visibility = View.GONE
    }

    private fun setupKeyboard() {
        val keys = listOf(
            R.id.key_1 to "1", R.id.key_2 to "2", R.id.key_3 to "3", R.id.key_4 to "4",
            R.id.key_5 to "5", R.id.key_6 to "6", R.id.key_7 to "7", R.id.key_8 to "8",
            R.id.key_9 to "9", R.id.key_0 to "0",
            R.id.key_q to "Q", R.id.key_w to "W", R.id.key_e to "E", R.id.key_r to "R",
            R.id.key_t to "T", R.id.key_y to "Y", R.id.key_u to "U", R.id.key_i to "I",
            R.id.key_o to "O", R.id.key_p to "P",
            R.id.key_a to "A", R.id.key_s to "S", R.id.key_d to "D", R.id.key_f to "F",
            R.id.key_g to "G", R.id.key_h to "H", R.id.key_j to "J", R.id.key_k to "K",
            R.id.key_l to "L",
            R.id.key_z to "Z", R.id.key_x to "X", R.id.key_c to "C", R.id.key_v to "V",
            R.id.key_b to "B", R.id.key_n to "N", R.id.key_m to "M",
            R.id.key_dash to "-", R.id.key_slash to "/", R.id.key_comma to ","
        )

        keys.forEach { (resId, char) ->
            findViewById<TextView>(resId)?.setOnClickListener {
                val target = activeEditText ?: return@setOnClickListener
                val currentText = target.text.toString()

                val maxLength = when (target.id) {
                    R.id.etMobileNumber -> 10
                    R.id.etDocumentId -> 10
                    R.id.etPincode -> 6
                    R.id.etDateOfBirth -> 10
                    else -> 100 // default high limit for Name/Address
                }

                if (currentText.length < maxLength) {
                    target.append(char)
                }
            }
        }

        // Space key
        findViewById<TextView>(R.id.key_space)?.setOnClickListener {
            val target = activeEditText ?: return@setOnClickListener
            target.append(" ")
        }

        // Backspace key
        findViewById<TextView>(R.id.key_backspace)?.setOnClickListener {
            val target = activeEditText ?: return@setOnClickListener
            val currentText = target.text.toString()
            if (currentText.isNotEmpty()) {
                target.setText(currentText.substring(0, currentText.length - 1))
                target.setSelection(target.text.length)
            }
        }

        // Clear key
        findViewById<TextView>(R.id.key_clear)?.setOnClickListener {
            activeEditText?.setText("")
        }

        // Hide key
        findViewById<TextView>(R.id.key_hide)?.setOnClickListener {
            binding.layoutKeyboard.visibility = View.GONE
            activeEditText?.clearFocus()
        }
    }

    private fun setupListeners() {
        // Back navigation
        binding.btnBackContainer.setOnClickListener {
            finish()
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
