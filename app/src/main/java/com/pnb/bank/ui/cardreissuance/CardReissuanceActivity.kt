package com.pnb.bank.ui.cardreissuance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pnb.bank.data.api.ApiRepository
import com.pnb.bank.data.api.NetworkResult
import com.pnb.bank.databinding.ActivityCardReissuanceBinding
import com.pnb.bank.databinding.LayoutNumericKeypadBinding
import com.pnb.bank.utils.AppLogger
import kotlinx.coroutines.launch

class CardReissuanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardReissuanceBinding
    private lateinit var keypadBinding: LayoutNumericKeypadBinding

    private var activeEditText: EditText? = null
    private var selectedCardVariant: String = ""
    private var selectedNameFormat: String = ""

    // API Session State
    private var savedTransactionId: String = "TXN72680A5056884609"
    private var savedAccountNumber: String = "666444222"
    private var savedCustomerName: String = "Nikhil Randive"

    private val apiRepository by lazy { ApiRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardReissuanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keypadBinding = LayoutNumericKeypadBinding.bind(binding.layoutKeypad.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = android.graphics.Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = android.graphics.Color.parseColor("#50000000")
        }

        setupFocusListeners()
        setupKeypadListeners()
        setupCardSelectionListeners()
        setupNameSelectionListeners()
        setupFormListeners()
        setupTextClearErrorListeners()
    }

    private fun setupFocusListeners() {
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
        binding.rgCardVariants.setOnCheckedChangeListener { _, checkedId ->
            selectedCardVariant = when (checkedId) {
                binding.rbRupaySelect.id -> "RuPay Select"
                binding.rbRupayClassic.id -> "RuPay Classic"
                binding.rbRupayPlatinum.id -> "RuPay Platinum"
                binding.rbVisaClassic.id -> "Visa International"
                else -> ""
            }

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
                binding.rbFirstNameLastName.id -> binding.rbFirstNameLastName.tag?.toString() ?: savedCustomerName
                binding.rbLastNameFirstName.id -> binding.rbLastNameFirstName.tag?.toString() ?: savedCustomerName
                binding.rbFirstInitialLastName.id -> binding.rbFirstInitialLastName.tag?.toString() ?: savedCustomerName
                binding.rbLastInitialFirstName.id -> binding.rbLastInitialFirstName.tag?.toString() ?: savedCustomerName
                else -> savedCustomerName
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
        val numberButtons = listOf(
            keypadBinding.btnKey0, keypadBinding.btnKey1, keypadBinding.btnKey2,
            keypadBinding.btnKey3, keypadBinding.btnKey4, keypadBinding.btnKey5,
            keypadBinding.btnKey6, keypadBinding.btnKey7, keypadBinding.btnKey8, keypadBinding.btnKey9
        )

        numberButtons.forEach { btn ->
            btn.setOnClickListener {
                val target = activeEditText ?: return@setOnClickListener
                val digit = btn.text.toString()
                val currentText = target.text.toString()

                val maxLength = if (target.id == binding.etAccountNumber.id) 16 else 6
                if (currentText.length < maxLength) {
                    target.append(digit)
                }
            }
        }

        keypadBinding.btnKeyClear.setOnClickListener {
            activeEditText?.setText("")
        }

        keypadBinding.btnKeyBackspace.setOnClickListener {
            val target = activeEditText ?: return@setOnClickListener
            val currentText = target.text.toString()
            if (currentText.isNotEmpty()) {
                target.setText(currentText.substring(0, currentText.length - 1))
                target.setSelection(target.text.length)
            }
        }
    }

    private fun setupFormListeners() {
        binding.btnAutoFillTest.setOnClickListener {
            binding.etAccountNumber.setText(com.pnb.bank.utils.AppConstants.TEST_ACCOUNT_NUMBER)
            binding.etPanNumber.setText(com.pnb.bank.utils.AppConstants.TEST_PAN_NUMBER)
            binding.tvAccountError.visibility = View.GONE
            binding.tvPanError.visibility = View.GONE
            Toast.makeText(this, "Test Account & PAN Auto-Filled!", Toast.LENGTH_SHORT).show()
        }

        binding.btnProceedContainer.setOnClickListener {
            if (binding.cardAccountStep.visibility == View.VISIBLE) {
                // Step 1: Validate Customer API (pg/api/v1/debitcard/validateCustomer)
                val accountNo = binding.etAccountNumber.text.toString().trim()
                val panNo = binding.etPanNumber.text.toString().trim()

                if (accountNo.isEmpty() || accountNo.length < 9) {
                    binding.tvAccountError.text = "Please enter valid Account Number"
                    binding.tvAccountError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                performCbsValidation(accountNo, panNo)
            } else if (binding.cardOtpStep.visibility == View.VISIBLE) {
                // Step 2: Verify OTP API (pg/api/v1/debitcard/verifyOtp)
                val otp = binding.etOtpNumber.text.toString().trim()

                if (otp.length != 6) {
                    binding.tvOtpError.text = "Please enter 6-digit OTP"
                    binding.tvOtpError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                performOtpVerification(otp)
            } else if (binding.cardSelectionStep.visibility == View.VISIBLE) {
                // Step 3: Card Variant Selection
                if (selectedCardVariant.isEmpty()) {
                    binding.tvCardSelectError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                binding.cardSelectionStep.visibility = View.GONE
                binding.cardNameSelectionStep.visibility = View.VISIBLE
            } else if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
                // Step 4: Link Card API (pg/api/v1/debitcard/linkCard)
                if (selectedNameFormat.isEmpty()) {
                    binding.tvNameSelectError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                performLinkCard()
            }
        }

        binding.btnHomeContainer.setOnClickListener {
            finishAffinity()
        }

        binding.btnBackContainer.setOnClickListener {
            handleCardReissuanceBackNavigation()
        }

        binding.tvResendOtp.setOnClickListener {
            val otp = binding.etOtpNumber.text.toString().trim()
            if (otp.isNotEmpty()) {
                performOtpVerification(otp)
            } else {
                Toast.makeText(this, "Requesting new OTP...", Toast.LENGTH_SHORT).show()
                performCbsValidation(savedAccountNumber, binding.etPanNumber.text.toString().trim())
            }
        }
    }

    private fun handleCardReissuanceBackNavigation() {
        if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
            binding.cardNameSelectionStep.visibility = View.GONE
            binding.cardSelectionStep.visibility = View.VISIBLE
        } else if (binding.cardSelectionStep.visibility == View.VISIBLE) {
            binding.cardSelectionStep.visibility = View.GONE
            binding.cardOtpStep.visibility = View.VISIBLE
            binding.cardKeypadContainer.visibility = View.VISIBLE
            activeEditText = binding.etOtpNumber
            binding.etOtpNumber.requestFocus()
        } else if (binding.cardOtpStep.visibility == View.VISIBLE) {
            binding.cardOtpStep.visibility = View.GONE
            binding.cardAccountStep.visibility = View.VISIBLE
            binding.btnHomeContainer.visibility = View.VISIBLE
            binding.btnBackContainer.visibility = View.GONE
            activeEditText = binding.etAccountNumber
            binding.etAccountNumber.requestFocus()
        } else {
            finishAffinity()
        }
    }

    private fun setProceedLoading(isLoading: Boolean, loadingText: String = "Please wait...") {
        binding.btnProceedContainer.isEnabled = !isLoading
        binding.btnProceedContainer.isClickable = !isLoading
        if (isLoading) {
            binding.pbProceedLoading.visibility = View.VISIBLE
            binding.btnProceed.text = loadingText
            binding.btnProceed.textSize = 20f
        } else {
            binding.pbProceedLoading.visibility = View.GONE
            binding.btnProceed.text = "Proceed"
            binding.btnProceed.textSize = 26f
        }
    }

    private fun performCbsValidation(accountNo: String, panNo: String) {
        setProceedLoading(true, "Validating...")
        binding.tvAccountError.visibility = View.GONE
        binding.tvPanError.visibility = View.GONE
        savedAccountNumber = accountNo

        lifecycleScope.launch {
            val result = apiRepository.validateCustomer(
                panNumber = panNo.ifEmpty { "ABCDE1234F" },
                accountNumber = accountNo
            )

            setProceedLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val isApiSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"

                    if (isApiSuccess) {
                        if (!response.responseId.isNullOrEmpty()) {
                            savedTransactionId = response.responseId
                        }
                        AppLogger.i("API 1 Success [validateCustomer]: TxnId=$savedTransactionId | Msg=${response.responseMessage}")

                        // NAVIGATE TO NEXT SCREEN ONLY ON SUCCESS
                        binding.cardAccountStep.visibility = View.GONE
                        binding.cardOtpStep.visibility = View.VISIBLE
                        binding.btnHomeContainer.visibility = View.GONE
                        binding.btnBackContainer.visibility = View.VISIBLE
                        activeEditText = binding.etOtpNumber
                        binding.etOtpNumber.requestFocus()

                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Generated Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        AppLogger.w("API 1 Response Error [validateCustomer]: ${response.responseMessage}")
                        binding.tvPanError.text = response.responseMessage ?: "Validation Failed"
                        binding.tvPanError.visibility = View.VISIBLE
                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "Validation Failed", Toast.LENGTH_LONG).show()
                    }
                }
                is NetworkResult.Error -> {
                    AppLogger.w("API 1 Network Error [validateCustomer]: ${result.message}")

                    // STAY ON CURRENT SCREEN ON FAILURE - Show error below PAN field
                    binding.tvPanError.text = result.message
                    binding.tvPanError.visibility = View.VISIBLE

                    Toast.makeText(this@CardReissuanceActivity, "Validation Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun performOtpVerification(otp: String) {
        setProceedLoading(true, "Verifying...")
        binding.tvOtpError.visibility = View.GONE

        lifecycleScope.launch {
            val result = apiRepository.verifyOtp(
                transactionId = savedTransactionId,
                otp = otp
            )

            setProceedLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val isApiSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"

                    if (isApiSuccess) {
                        savedCustomerName = response.customerName ?: "Nikhil Randive"
                        AppLogger.i("API 2 Success [verifyOtp]: CustomerName=$savedCustomerName | Status=${response.status} | Variants=${response.eligibleCardVariants}")

                        updateEligibleCardVariants(response.eligibleCardVariants)
                        updateCustomerNameOptions(savedCustomerName)

                        // NAVIGATE TO NEXT SCREEN ONLY ON SUCCESS
                        binding.cardOtpStep.visibility = View.GONE
                        binding.cardKeypadContainer.visibility = View.GONE
                        binding.cardSelectionStep.visibility = View.VISIBLE

                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        AppLogger.w("API 2 Response Error [verifyOtp]: ${response.responseMessage}")
                        binding.tvOtpError.text = response.responseMessage ?: "OTP Verification Failed"
                        binding.tvOtpError.visibility = View.VISIBLE
                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Verification Failed", Toast.LENGTH_LONG).show()
                    }
                }
                is NetworkResult.Error -> {
                    AppLogger.w("API 2 Network Error [verifyOtp]: ${result.message}")

                    // STAY ON CURRENT SCREEN ON FAILURE
                    binding.tvOtpError.text = result.message
                    binding.tvOtpError.visibility = View.VISIBLE

                    Toast.makeText(this@CardReissuanceActivity, "OTP Verification Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun updateCustomerNameOptions(fullName: String) {
        val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val firstName = parts.firstOrNull() ?: ""
        val lastName = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""

        val opt1Text: String
        val opt2Text: String
        val opt3Text: String
        val opt4Text: String

        if (lastName.isNotEmpty()) {
            val firstInitial = firstName.take(1).uppercase()
            val lastInitial = parts.last().take(1).uppercase()

            opt1Text = "$firstName $lastName"
            opt2Text = "$lastName $firstName"
            opt3Text = "$firstInitial. $lastName"
            opt4Text = "$lastInitial. $firstName"
        } else {
            opt1Text = firstName
            opt2Text = firstName
            opt3Text = firstName
            opt4Text = firstName
        }

        binding.rbFirstNameLastName.text = "👤 First Name Last Name\n$opt1Text"
        binding.rbLastNameFirstName.text = "👤 Last Name First Name\n$opt2Text"
        binding.rbFirstInitialLastName.text = "🔤 First Initial Last Name\n$opt3Text"
        binding.rbLastInitialFirstName.text = "🔤 Last Initial First Name\n$opt4Text"

        binding.rbFirstNameLastName.tag = opt1Text
        binding.rbLastNameFirstName.tag = opt2Text
        binding.rbFirstInitialLastName.tag = opt3Text
        binding.rbLastInitialFirstName.tag = opt4Text

        binding.rbFirstNameLastName.isChecked = true
        selectedNameFormat = opt1Text
    }

    private fun updateEligibleCardVariants(eligibleVariants: List<String>?) {
        if (eligibleVariants.isNullOrEmpty()) {
            binding.rbRupaySelect.visibility = View.VISIBLE
            binding.rbRupayClassic.visibility = View.VISIBLE
            binding.rbRupayPlatinum.visibility = View.VISIBLE
            binding.rbVisaClassic.visibility = View.VISIBLE
            return
        }

        val hasRupaySelect = eligibleVariants.any { it.equals("RuPay Select", ignoreCase = true) || it.equals("RUPAY_SELECT", ignoreCase = true) }
        val hasRupayClassic = eligibleVariants.any { it.equals("RuPay Classic", ignoreCase = true) || it.equals("RUPAY_CLASSIC", ignoreCase = true) }
        val hasRupayPlatinum = eligibleVariants.any { it.equals("RuPay Platinum", ignoreCase = true) || it.equals("RUPAY_PLATINUM", ignoreCase = true) }
        val hasVisaClassic = eligibleVariants.any { it.equals("Visa International", ignoreCase = true) || it.contains("Visa", ignoreCase = true) }

        binding.rbRupaySelect.visibility = if (hasRupaySelect) View.VISIBLE else View.GONE
        binding.rbRupayClassic.visibility = if (hasRupayClassic) View.VISIBLE else View.GONE
        binding.rbRupayPlatinum.visibility = if (hasRupayPlatinum) View.VISIBLE else View.GONE
        binding.rbVisaClassic.visibility = if (hasVisaClassic) View.VISIBLE else View.GONE

        // Automatically pre-check the first available card option
        binding.rgCardVariants.clearCheck()
        when {
            hasRupaySelect -> {
                binding.rbRupaySelect.isChecked = true
                selectedCardVariant = "RuPay Select"
            }
            hasRupayClassic -> {
                binding.rbRupayClassic.isChecked = true
                selectedCardVariant = "RuPay Classic"
            }
            hasRupayPlatinum -> {
                binding.rbRupayPlatinum.isChecked = true
                selectedCardVariant = "RuPay Platinum"
            }
            hasVisaClassic -> {
                binding.rbVisaClassic.isChecked = true
                selectedCardVariant = "Visa International"
            }
            else -> {
                binding.rbRupaySelect.visibility = View.VISIBLE
                binding.rbRupayClassic.visibility = View.VISIBLE
                binding.rbRupaySelect.isChecked = true
                selectedCardVariant = "RuPay Select"
            }
        }
    }

    private fun performLinkCard() {
        setProceedLoading(true, "Linking...")

        lifecycleScope.launch {
            val variantCode = selectedCardVariant
                .trim()
                .uppercase()
                .replace("\\s+".toRegex(), "_")

            val result = apiRepository.linkCard(
                accountNumber = savedAccountNumber,
                cardNumber = com.pnb.bank.data.api.ApiConstants.activeCardNumber,
                cardVariant = variantCode,
                customerName = if (selectedNameFormat.isNotEmpty()) selectedNameFormat else savedCustomerName
            )

            setProceedLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val isApiSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"

                    if (isApiSuccess) {
                        AppLogger.i("API 3 Success [linkCard]: Status=${response.status} | CardStatus=${response.cardStatus}")

                        // NAVIGATE TO SUCCESS SCREEN ONLY ON SUCCESS
                        binding.cardNameSelectionStep.visibility = View.GONE
                        binding.cardSuccessStep.visibility = View.VISIBLE
                        binding.btnProceedContainer.visibility = View.GONE

                        binding.tvSuccessDetails.text = "Card Status: ${response.cardStatus ?: "ACTIVE"}\n${if (selectedNameFormat.isNotEmpty()) selectedNameFormat else savedCustomerName}'s new $selectedCardVariant Debit Card has been linked successfully."
                        Toast.makeText(this@CardReissuanceActivity, "Debit Card Linked & Issued Successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        AppLogger.w("API 3 Response Error [linkCard]: Status=${response.status}")
                        Toast.makeText(this@CardReissuanceActivity, "Card Link Failed: Status ${response.status}", Toast.LENGTH_LONG).show()
                    }
                }
                is NetworkResult.Error -> {
                    AppLogger.w("API 3 Network Error [linkCard]: ${result.message}")

                    Toast.makeText(this@CardReissuanceActivity, "Card Link Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
