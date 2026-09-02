package com.pnb.bank.ui.ckyc

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.R
import com.pnb.bank.databinding.ActivityCkycBinding
import com.pnb.bank.utils.hideSystemUI
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CkycActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCkycBinding
    private var activeEditText: EditText? = null
    private lateinit var apiRepository: com.pnb.bank.data.api.ApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityCkycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiRepository = com.pnb.bank.data.api.ApiRepository()

        // Premium shadows for Android P and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = Color.parseColor("#50000000")
        }

        setupFocusListeners()
        setupQwertyKeyboard()
        setupListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun setupFocusListeners() {
        val editText = binding.etAccountNumber

        // Prevent the soft keyboard from popping up automatically
        editText.showSoftInputOnFocus = false

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                activeEditText = editText
                switchKeyboardForFocus()
            }
        }

        editText.setOnClickListener {
            activeEditText = editText
            switchKeyboardForFocus()
        }

        // Initially show the Info Banner and hide the keyboard panel
        activeEditText = null
        binding.layoutKeyboard.visibility = View.GONE
        binding.layoutGuideBanner.visibility = View.VISIBLE
    }

    private fun switchKeyboardForFocus() {
        // Swap panels: Hide guide banner, Show QWERTY keyboard container
        binding.layoutGuideBanner.visibility = View.GONE
        binding.layoutKeyboard.visibility = View.VISIBLE
        binding.containerQwerty.visibility = View.VISIBLE
        
        // Explicitly hide native soft keyboard
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun hideKeyboardAndShowBanner() {
        binding.layoutKeyboard.visibility = View.GONE
        binding.layoutGuideBanner.visibility = View.VISIBLE
        activeEditText?.clearFocus()
        activeEditText = null
    }

    private fun appendDigitOrChar(target: EditText, value: String) {
        val currentText = target.text.toString()

        val maxLength = 16 // For Account Number

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
            R.id.key_at to "@", R.id.key_dot to ".", R.id.key_underscore to "_"
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

    private fun setupListeners() {
        // Back navigation
        binding.btnBackContainer.setOnClickListener {
            finish()
        }

        // Submit Button click
        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }

        // Test Data click
        binding.btnTestData.setOnClickListener {
            binding.etAccountNumber.setText("015300MD00000473")
            android.widget.Toast.makeText(this, "Autofilled with test Account Number", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Done button on Success Screen
        binding.btnDone.setOnClickListener {
            finish() // Goes back to the previous screen (Home)
        }
    }

    private fun validateAndSubmit() {
        val accountNo = binding.etAccountNumber.text.toString().trim()

        if (accountNo.isEmpty()) {
            showError("Please enter the Account Number to proceed.")
            return
        }

        if (accountNo.length < 10) {
            showError("Please enter a valid Account Number.")
            return
        }

        // Clear error if any
        binding.tvFormError.visibility = View.GONE

        // Trigger Button Loading State
        binding.tvSubmitText.visibility = View.GONE
        binding.pbSubmitLoading.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.6f

        lifecycleScope.launch {
            val result = apiRepository.fetchCkycNumber(accountNo)

            // Restore Button Normal State
            binding.tvSubmitText.visibility = View.VISIBLE
            binding.pbSubmitLoading.visibility = View.GONE
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.alpha = 1.0f

            when (result) {
                is com.pnb.bank.data.api.NetworkResult.Success -> {
                    val response = result.data
                    if (response.status == "S" && !response.ckycNum.isNullOrEmpty()) {
                        // Success - show success overlay
                        binding.tvCkycNumber.text = "CKYC No: ${response.ckycNum}"
                        binding.layoutSuccessOverlay.visibility = View.VISIBLE
                        hideKeyboardAndShowBanner()
                    } else {
                        // Handle failure case based on status F
                        val errorMsg = response.remarks ?: "Failed to get CKYC details."
                        showError("Failed: $errorMsg")
                    }
                }
                is com.pnb.bank.data.api.NetworkResult.Error -> {
                    showError(result.message ?: "Failed to connect. Please try again.")
                }
                is com.pnb.bank.data.api.NetworkResult.Loading -> {
                    // handled
                }
            }
        }
    }

    private fun showError(errorMsg: String) {
        binding.tvFormError.text = errorMsg
        binding.tvFormError.visibility = View.VISIBLE
    }
}
