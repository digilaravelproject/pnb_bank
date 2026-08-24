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
        setupQwertyKeyboard()
        setupNumericKeyboard()
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
            binding.etDocumentId
        )

        editTexts.forEach { editText ->
            // Prevent the soft keyboard from popping up automatically
            editText.showSoftInputOnFocus = false

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeEditText = editText
                    switchKeyboardForFocus(editText)
                }
            }

            editText.setOnClickListener {
                activeEditText = editText
                switchKeyboardForFocus(editText)
            }
        }

        // Initially show the Info Banner and hide the keyboard panel
        activeEditText = null
        binding.layoutKeyboard.visibility = View.GONE
        binding.layoutGuideBanner.visibility = View.VISIBLE
    }

    private fun switchKeyboardForFocus(editText: EditText) {
        // Swap panels: Hide guide banner, Show keyboard container
        binding.layoutGuideBanner.visibility = View.GONE
        binding.layoutKeyboard.visibility = View.VISIBLE

        if (editText.id == R.id.etMobileNumber) {
            binding.containerQwerty.visibility = View.GONE
            binding.containerNumeric.visibility = View.VISIBLE
        } else {
            binding.containerQwerty.visibility = View.VISIBLE
            binding.containerNumeric.visibility = View.GONE
        }
    }

    private fun hideKeyboardAndShowBanner() {
        binding.layoutKeyboard.visibility = View.GONE
        binding.layoutGuideBanner.visibility = View.VISIBLE
        activeEditText?.clearFocus()
        activeEditText = null
    }

    private fun appendDigitOrChar(target: EditText, value: String) {
        val currentText = target.text.toString()

        val maxLength = when (target.id) {
            R.id.etMobileNumber -> 10
            R.id.etDocumentId -> 10
            else -> 100 // default high limit (Name)
        }

        if (currentText.length < maxLength) {
            target.append(value)
        }
    }

    private fun performBackspace(target: EditText) {
        val currentText = target.text.toString()
        if (currentText.isNotEmpty()) {
            target.setText(currentText.substring(0, currentText.length - 1))
            target.setSelection(target.text.length)
        }
    }

    private fun setupQwertyKeyboard() {
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
                appendDigitOrChar(target, char)
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
            performBackspace(target)
        }

        // Clear key
        findViewById<TextView>(R.id.key_clear)?.setOnClickListener {
            activeEditText?.setText("")
        }

        // Hide key
        findViewById<TextView>(R.id.key_hide)?.setOnClickListener {
            hideKeyboardAndShowBanner()
        }
    }

    private fun setupNumericKeyboard() {
        val numKeys = listOf(
            R.id.btnKey1 to "1", R.id.btnKey2 to "2", R.id.btnKey3 to "3",
            R.id.btnKey4 to "4", R.id.btnKey5 to "5", R.id.btnKey6 to "6",
            R.id.btnKey7 to "7", R.id.btnKey8 to "8", R.id.btnKey9 to "9",
            R.id.btnKey0 to "0"
        )

        numKeys.forEach { (resId, digit) ->
            findViewById<TextView>(resId)?.setOnClickListener {
                val target = activeEditText ?: return@setOnClickListener
                appendDigitOrChar(target, digit)
            }
        }

        // Numeric Backspace (⌫)
        findViewById<TextView>(R.id.btnKeyBackspace)?.setOnClickListener {
            val target = activeEditText ?: return@setOnClickListener
            performBackspace(target)
        }

        // Numeric Clear (✕) - Clears text and closes keyboard to show banner
        findViewById<TextView>(R.id.btnKeyClear)?.setOnClickListener {
            activeEditText?.setText("")
            hideKeyboardAndShowBanner()
        }
    }

    private fun setupListeners() {
        // Back navigation
        binding.btnBackContainer.setOnClickListener {
            finish()
        }

        // Submit Button click
        binding.btnSubmitCreditScore.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etFullName.text.toString().trim()
        val mobile = binding.etMobileNumber.text.toString().trim()
        val documentId = binding.etDocumentId.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || documentId.isEmpty()) {
            showError("Please fill all the details to proceed.")
            return
        }

        if (mobile.length != 10) {
            showError("Please enter a valid 10-digit mobile number.")
            return
        }

        if (documentId.length != 10) {
            showError("Please enter a valid 10-character PAN card number.")
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
