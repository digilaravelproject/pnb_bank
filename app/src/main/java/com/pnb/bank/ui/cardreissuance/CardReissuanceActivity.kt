package com.pnb.bank.ui.cardreissuance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.pnb.bank.databinding.ActivityCardReissuanceBinding
import com.pnb.bank.databinding.LayoutNumericKeypadBinding

class CardReissuanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardReissuanceBinding
    private lateinit var keypadBinding: LayoutNumericKeypadBinding

    private var activeEditText: EditText? = null
    private var selectedCardVariant: String = ""
    private var selectedNameFormat: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardReissuanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keypadBinding = LayoutNumericKeypadBinding.bind(binding.layoutKeypad.root)

        setupFocusListeners()
        setupKeypadListeners()
        setupCardSelectionListeners()
        setupNameSelectionListeners()
        setupFormListeners()
        setupTextClearErrorListeners()
    }

    private fun setupFocusListeners() {
        // Disable soft system keyboard on click/focus
        binding.etAccountNumber.showSoftInputOnFocus = false
        binding.etPanNumber.showSoftInputOnFocus = false
        binding.etOtpNumber.showSoftInputOnFocus = false

        activeEditText = binding.etAccountNumber

        binding.etAccountNumber.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) activeEditText = binding.etAccountNumber
        }
        binding.etPanNumber.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) activeEditText = binding.etPanNumber
        }
        binding.etOtpNumber.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) activeEditText = binding.etOtpNumber
        }
    }

    private fun setupCardSelectionListeners() {
        binding.rgCardVariants.setOnCheckedChangeListener { group, checkedId ->
            selectedCardVariant = when (checkedId) {
                binding.rbRupaySelect.id -> "RuPay Select"
                binding.rbRupayClassic.id -> "RuPay Classic"
                binding.rbRupayPlatinum.id -> "RuPay Platinum"
                binding.rbVisaClassic.id -> "Visa International"
                else -> ""
            }

            // Update selection background state on RadioButtons
            binding.rbRupaySelect.isSelected = (checkedId == binding.rbRupaySelect.id)
            binding.rbRupayClassic.isSelected = (checkedId == binding.rbRupayClassic.id)
            binding.rbRupayPlatinum.isSelected = (checkedId == binding.rbRupayPlatinum.id)
            binding.rbVisaClassic.isSelected = (checkedId == binding.rbVisaClassic.id)

            binding.tvCardSelectError.visibility = View.GONE
        }
    }

    private fun setupNameSelectionListeners() {
        binding.rgNameVariants.setOnCheckedChangeListener { _, checkedId ->
            selectedNameFormat = when (checkedId) {
                binding.rbFirstNameLastName.id -> "First Name Last Name"
                binding.rbLastNameFirstName.id -> "Last Name First Name"
                binding.rbFirstInitialLastName.id -> "First Initial Last Name"
                binding.rbLastInitialFirstName.id -> "Last Initial First Name"
                else -> ""
            }

            binding.rbFirstNameLastName.isSelected = (checkedId == binding.rbFirstNameLastName.id)
            binding.rbLastNameFirstName.isSelected = (checkedId == binding.rbLastNameFirstName.id)
            binding.rbFirstInitialLastName.isSelected = (checkedId == binding.rbFirstInitialLastName.id)
            binding.rbLastInitialFirstName.isSelected = (checkedId == binding.rbLastInitialFirstName.id)

            binding.tvNameSelectError.visibility = View.GONE
        }
    }

    private fun setupTextClearErrorListeners() {
        binding.etAccountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvAccountError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPanNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvPanError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etOtpNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvOtpError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupKeypadListeners() {
        val numberButtons = mapOf(
            keypadBinding.btnKey0 to "0",
            keypadBinding.btnKey1 to "1",
            keypadBinding.btnKey2 to "2",
            keypadBinding.btnKey3 to "3",
            keypadBinding.btnKey4 to "4",
            keypadBinding.btnKey5 to "5",
            keypadBinding.btnKey6 to "6",
            keypadBinding.btnKey7 to "7",
            keypadBinding.btnKey8 to "8",
            keypadBinding.btnKey9 to "9"
        )

        numberButtons.forEach { (view, value) ->
            view.setOnClickListener {
                appendCharacter(value)
            }
        }

        keypadBinding.btnKeyBackspace.setOnClickListener {
            removeLastCharacter()
        }

        keypadBinding.btnKeyClear.setOnClickListener {
            activeEditText?.setText("")
        }
    }

    private fun appendCharacter(char: String) {
        val editText = activeEditText ?: return
        val currentText = editText.text.toString()

        // Check InputFilter maxLength if present
        val maxLenFilter = editText.filters.filterIsInstance<android.text.InputFilter.LengthFilter>().firstOrNull()
        val maxLen = maxLenFilter?.max ?: Int.MAX_VALUE

        if (currentText.length >= maxLen) {
            return // Prevent typing past limit to avoid IndexOutOfBoundsException crash
        }

        val selectionStart = editText.selectionStart.coerceIn(0, currentText.length)
        val selectionEnd = editText.selectionEnd.coerceIn(0, currentText.length)

        val newText = currentText.replaceRange(selectionStart, selectionEnd, char)
        if (newText.length <= maxLen) {
            editText.setText(newText)
            val newCursorPos = (selectionStart + char.length).coerceAtMost(newText.length)
            editText.setSelection(newCursorPos)
        }
    }

    private fun removeLastCharacter() {
        val editText = activeEditText ?: return
        val currentText = editText.text.toString()
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        if (selectionStart != selectionEnd) {
            val newText = currentText.removeRange(selectionStart, selectionEnd)
            editText.setText(newText)
            editText.setSelection(selectionStart)
        } else if (selectionStart > 0) {
            val newText = currentText.removeRange(selectionStart - 1, selectionStart)
            editText.setText(newText)
            editText.setSelection(selectionStart - 1)
        }
    }

    private fun setupFormListeners() {
        // Bottom Right "Proceed" Action Button
        binding.btnProceed.setOnClickListener {
            // Hide previous errors
            binding.tvAccountError.visibility = View.GONE
            binding.tvPanError.visibility = View.GONE
            binding.tvOtpError.visibility = View.GONE
            binding.tvCardSelectError.visibility = View.GONE
            binding.tvNameSelectError.visibility = View.GONE

            if (binding.cardAccountStep.visibility == View.VISIBLE) {
                // Step 1: Validate Customer via CBS
                val accountNo = binding.etAccountNumber.text.toString().trim()
                val panNo = binding.etPanNumber.text.toString().trim()

                if (accountNo.isEmpty() || accountNo.length < 10) {
                    binding.tvAccountError.text = "Please enter valid Account Number"
                    binding.tvAccountError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                performCbsValidation(accountNo, panNo)
            } else if (binding.cardOtpStep.visibility == View.VISIBLE) {
                // Step 2: Verify OTP
                val otp = binding.etOtpNumber.text.toString().trim()

                if (otp.length != 6) {
                    binding.tvOtpError.text = "Please enter 6-digit OTP"
                    binding.tvOtpError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                performOtpVerification(otp)
            } else if (binding.cardSelectionStep.visibility == View.VISIBLE) {
                // Step 3: Card Selection -> Go to Display Name Printing Options
                if (selectedCardVariant.isEmpty()) {
                    binding.tvCardSelectError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                binding.cardSelectionStep.visibility = View.GONE
                binding.cardNameSelectionStep.visibility = View.VISIBLE
            } else if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
                // Step 4: Name Selection -> Final Confirmation
                if (selectedNameFormat.isEmpty()) {
                    binding.tvNameSelectError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                binding.cardNameSelectionStep.visibility = View.GONE
                binding.cardSuccessStep.visibility = View.VISIBLE
                binding.btnProceed.visibility = View.GONE

                binding.tvSuccessDetails.text = "Your new $selectedCardVariant Debit Card request ($selectedNameFormat) has been successfully submitted."
                Toast.makeText(this, "$selectedCardVariant Request Submitted Successfully!", Toast.LENGTH_LONG).show()
            }
        }

        // Bottom Left Home / Back Action Button Container
        binding.btnHomeContainer.setOnClickListener {
            if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
                // Step-back from Name Selection to Card Selection Step 3
                binding.cardNameSelectionStep.visibility = View.GONE
                binding.cardSelectionStep.visibility = View.VISIBLE
            } else if (binding.cardSelectionStep.visibility == View.VISIBLE) {
                // Step-back from Card Selection to OTP Step 2
                binding.cardSelectionStep.visibility = View.GONE
                binding.cardOtpStep.visibility = View.VISIBLE
                binding.cardKeypadContainer.visibility = View.VISIBLE
                activeEditText = binding.etOtpNumber
                binding.etOtpNumber.requestFocus()
            } else if (binding.cardOtpStep.visibility == View.VISIBLE) {
                // Step-back from OTP screen to Account Step 1 screen
                binding.cardOtpStep.visibility = View.GONE
                binding.cardAccountStep.visibility = View.VISIBLE
                binding.ivHomeIcon.visibility = View.VISIBLE
                binding.btnBack.visibility = View.GONE
                activeEditText = binding.etAccountNumber
                binding.etAccountNumber.requestFocus()
            } else {
                // First Screen Home action: Finish activity
                finish()
            }
        }

        // Resend OTP
        binding.tvResendOtp.setOnClickListener {
            Toast.makeText(this, "A new OTP has been sent to your registered mobile number.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performCbsValidation(accountNo: String, panNo: String) {
        binding.btnProceed.isEnabled = false
        binding.layoutCbsLoading.visibility = View.VISIBLE

        binding.root.postDelayed({
            binding.layoutCbsLoading.visibility = View.GONE
            binding.btnProceed.isEnabled = true

            // Transition to Step 2: OTP Validation Screen
            binding.cardAccountStep.visibility = View.GONE
            binding.cardOtpStep.visibility = View.VISIBLE
            binding.ivHomeIcon.visibility = View.GONE
            binding.btnBack.visibility = View.VISIBLE
            activeEditText = binding.etOtpNumber
            binding.etOtpNumber.requestFocus()

            Toast.makeText(this, "CBS Details Validated. OTP generated & sent!", Toast.LENGTH_SHORT).show()
        }, 1500)
    }

    private fun performOtpVerification(otp: String) {
        binding.btnProceed.isEnabled = false
        binding.layoutOtpLoading.visibility = View.VISIBLE

        binding.root.postDelayed({
            binding.layoutOtpLoading.visibility = View.GONE
            binding.btnProceed.isEnabled = true

            // Transition to Step 3: Card Selection Screen
            binding.cardOtpStep.visibility = View.GONE
            binding.cardKeypadContainer.visibility = View.GONE
            binding.cardSelectionStep.visibility = View.VISIBLE

            Toast.makeText(this, "OTP Verified! Select Card Variant.", Toast.LENGTH_SHORT).show()
        }, 1500)
    }
}
