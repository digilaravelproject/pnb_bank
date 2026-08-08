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
    private var cardAnimatorSet: android.animation.AnimatorSet? = null

    // API Session State (Populated dynamically from user input & API 1 responseId)
    private var savedTransactionId: String = ""
    private var savedAccountNumber: String = ""
    private var savedCustomerName: String = ""
    private var savedEligibleVariants: List<String> = emptyList()

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
            android.util.Log.d("PNB_TEST", "AutoFill button clicked!")
            showTestAccountSelectionDialog()
        }

        binding.tvResendOtp.setOnClickListener {
            val accountNo = if (savedAccountNumber.isNotEmpty()) savedAccountNumber else binding.etAccountNumber.text.toString().trim()
            val panNo = binding.etPanNumber.text.toString().trim()

            binding.etOtpNumber.setText("")
            binding.tvOtpError.visibility = View.GONE

            if (accountNo.isNotEmpty()) {
                Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
                performCbsValidation(accountNo, panNo)
            } else {
                Toast.makeText(this, "OTP Resent Successfully!", Toast.LENGTH_SHORT).show()
            }
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
                if (otp.isEmpty()) {
                    binding.tvOtpError.text = "Please enter OTP"
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
                // Step 4: Name Variant Selected -> Fetch Card Number & Show Confirmation Screen
                if (selectedNameFormat.isEmpty()) {
                    binding.tvNameSelectError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                showConfirmationScreen()
            } else if (binding.cardConfirmationStep.visibility == View.VISIBLE) {
                // Step 5: Confirmed -> Execute Link Card API (pg/api/v1/debitcard/linkCard)
                performLinkCard()
            }
        }

        binding.btnHomeContainer.setOnClickListener {
            finishAffinity()
        }

        binding.btnBackContainer.setOnClickListener {
            handleCardReissuanceBackNavigation()
        }
    }

    private fun showTestAccountSelectionDialog() {
        val testAccounts = com.pnb.bank.utils.AppConstants.TEST_ACCOUNTS_LIST
        val options = testAccounts.map { "Acc: ${it.accountNumber}   |   PAN: ${it.panNumber}" }.toTypedArray()

        val contextWrapper = android.view.ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
        androidx.appcompat.app.AlertDialog.Builder(contextWrapper)
            .setTitle("Select Test Account")
            .setItems(options) { _, which ->
                val selectedAccount = testAccounts[which]
                binding.etAccountNumber.setText(selectedAccount.accountNumber)
                binding.etPanNumber.setText(selectedAccount.panNumber)
                binding.tvAccountError.visibility = View.GONE
                binding.tvPanError.visibility = View.GONE

                binding.btnAutoFillTest.text = "⚡ Auto-Fill (${selectedAccount.accountNumber} / ${selectedAccount.panNumber})"
                Toast.makeText(this, "Selected: Acc ${selectedAccount.accountNumber}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCardReissuanceBackNavigation() {
        if (binding.cardConfirmationStep.visibility == View.VISIBLE) {
            binding.cardConfirmationStep.visibility = View.GONE
            binding.cardNameSelectionStep.visibility = View.VISIBLE
        } else if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
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
            binding.etOtpNumber.setText("")
            binding.tvOtpError.visibility = View.GONE
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
                        val txnId = when {
                            !response.transactionId.isNullOrEmpty() && !response.transactionId.equals("null", ignoreCase = true) -> response.transactionId
                            !response.responseId.isNullOrEmpty() && !response.responseId.equals("null", ignoreCase = true) -> response.responseId
                            else -> savedTransactionId
                        }
                        savedTransactionId = txnId

                        // Dynamic Mobile Number Masking for OTP Screen Subtitle
                        val rawMobile = response.customerMobileNumber?.trim()
                        if (!rawMobile.isNullOrEmpty() && !rawMobile.equals("null", ignoreCase = true)) {
                            val maskedMobile = if (rawMobile.length >= 4) {
                                "*".repeat(rawMobile.length - 4) + rawMobile.takeLast(4)
                            } else {
                                "********" + rawMobile
                            }
                            binding.tvOtpSubtitle.text = "OTP has been sent to your registered mobile number $maskedMobile"
                        } else {
                            binding.tvOtpSubtitle.text = "OTP has been sent to your registered mobile number ********89"
                        }

                        // Extract Customer Name from API 1 if present and valid (filter out "null" strings)
                        val nameParts = listOfNotNull(response.firstName, response.middleName, response.lastName)
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

                        if (nameParts.isNotEmpty()) {
                            savedCustomerName = nameParts.joinToString(" ")
                            AppLogger.i("API 1 Customer Name Extracted: $savedCustomerName")
                        }

                        AppLogger.i("API 1 Success [validateCustomer]: TxnId=$savedTransactionId | Mobile=${response.customerMobileNumber} | Msg=${response.responseMessage}")

                        // NAVIGATE TO NEXT STEP ONLY ON SUCCESS
                        binding.cardAccountStep.visibility = View.GONE
                        binding.cardOtpStep.visibility = View.VISIBLE
                        binding.btnHomeContainer.visibility = View.GONE
                        binding.btnBackContainer.visibility = View.VISIBLE
                        binding.etOtpNumber.setText("")
                        binding.tvOtpError.visibility = View.GONE
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

        val txnId = if (savedTransactionId.isNotEmpty()) savedTransactionId else "TXN72680A5056884609"

        lifecycleScope.launch {
            val result = apiRepository.verifyOtp(
                transactionId = txnId,
                otp = otp
            )

            setProceedLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val isApiSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"

                    if (isApiSuccess) {
                        savedCustomerName = if (!response.customerName.isNullOrEmpty()) response.customerName else "Nikhil Randive"
                        savedEligibleVariants = if (!response.eligibleCardVariants.isNullOrEmpty()) response.eligibleCardVariants else listOf("RuPay Classic", "RuPay Select")

                        AppLogger.i("API 2 Success [verifyOtp]: CustomerName=$savedCustomerName | Status=${response.status} | Variants=$savedEligibleVariants")

                        updateEligibleCardVariants(savedEligibleVariants)
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
        
        val firstName: String
        val lastName: String

        if (parts.size >= 2) {
            // If name has 2 or more words (e.g. Abhay Kumar Singh), take ONLY first and last word (Abhay Singh)
            firstName = parts.first()
            lastName = parts.last()
        } else if (parts.isNotEmpty()) {
            firstName = parts.first()
            lastName = ""
        } else {
            firstName = ""
            lastName = ""
        }

        val opt1Text: String
        val opt2Text: String
        val opt3Text: String
        val opt4Text: String

        if (lastName.isNotEmpty()) {
            // 2 or more words -> Show 4 distinct formatting variations
            val firstInitial = firstName.take(1).uppercase()
            val lastInitial = lastName.take(1).uppercase()

            val opt1Text = "$firstName $lastName"
            val opt2Text = "$lastName $firstName"
            val opt3Text = "$firstInitial. $lastName"
            val opt4Text = "$lastInitial. $firstName"

            binding.rbFirstNameLastName.text = "👤 First Name Last Name\n$opt1Text"
            binding.rbLastNameFirstName.text = "👤 Last Name First Name\n$opt2Text"
            binding.rbFirstInitialLastName.text = "👤 First Initial Last Name\n$opt3Text"
            binding.rbLastInitialFirstName.text = "👤 Last Initial First Name\n$opt4Text"

            binding.rbFirstNameLastName.tag = opt1Text
            binding.rbLastNameFirstName.tag = opt2Text
            binding.rbFirstInitialLastName.tag = opt3Text
            binding.rbLastInitialFirstName.tag = opt4Text

            binding.rbFirstNameLastName.visibility = View.VISIBLE
            binding.rbLastNameFirstName.visibility = View.VISIBLE
            binding.rbFirstInitialLastName.visibility = View.VISIBLE
            binding.rbLastInitialFirstName.visibility = View.VISIBLE

            binding.rbFirstNameLastName.isChecked = true
            selectedNameFormat = opt1Text
        } else {
            // Single word name (e.g. "Abhay" or "Singh") -> Show ONLY 1 option, hide duplicate options
            val opt1Text = firstName.ifEmpty { "CUSTOMER NAME" }

            binding.rbFirstNameLastName.text = "👤 Full Name\n$opt1Text"
            binding.rbFirstNameLastName.tag = opt1Text

            binding.rbFirstNameLastName.visibility = View.VISIBLE
            binding.rbLastNameFirstName.visibility = View.GONE
            binding.rbFirstInitialLastName.visibility = View.GONE
            binding.rbLastInitialFirstName.visibility = View.GONE

            binding.rbFirstNameLastName.isChecked = true
            selectedNameFormat = opt1Text
        }
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

    private fun showConfirmationScreen() {
        val fetchedCardNumber = com.pnb.bank.data.api.ApiConstants.activeCardNumber
        val displayName = if (selectedNameFormat.isNotEmpty()) selectedNameFormat else savedCustomerName

        // Populate Front & Back Debit Card Mockups
        binding.tvCardMockupName.text = displayName.uppercase()
        binding.tvCardMockupVariant.text = if (selectedCardVariant.contains("SELECT", ignoreCase = true)) "Select" else selectedCardVariant.uppercase()

        val formattedCardNum = if (fetchedCardNumber.length == 16) {
            "${fetchedCardNumber.substring(0, 4)}  ${fetchedCardNumber.substring(4, 8)}  ${fetchedCardNumber.substring(8, 12)}  ${fetchedCardNumber.substring(12, 16)}"
        } else {
            fetchedCardNumber
        }
        binding.tvCardMockupNumber.text = formattedCardNum
        binding.tvCardBackMockupNumber.text = formattedCardNum

        binding.cardNameSelectionStep.visibility = View.GONE
        binding.cardConfirmationStep.visibility = View.VISIBLE
        startDebitCardAnimation()
    }

    private fun startDebitCardAnimation() {
        val frontCard = binding.rlDebitCardMockup
        val backCard = binding.rlDebitCardMockupBack
        val shimmerView = binding.vShimmerOverlay

        val translateYFront = android.animation.ObjectAnimator.ofFloat(frontCard, "translationY", 0f, -6f, 0f).apply {
            duration = 3200
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.RESTART
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }

        val translateYBack = android.animation.ObjectAnimator.ofFloat(backCard, "translationY", 0f, -6f, 0f).apply {
            duration = 3200
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.RESTART
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            startDelay = 400
        }

        val tiltFront = android.animation.ObjectAnimator.ofFloat(frontCard, "rotationY", -2f, 2f, -2f).apply {
            duration = 4500
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.RESTART
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }

        val tiltBack = android.animation.ObjectAnimator.ofFloat(backCard, "rotationY", 2f, -2f, 2f).apply {
            duration = 4500
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.RESTART
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            startDelay = 400
        }

        cardAnimatorSet?.cancel()
        cardAnimatorSet = android.animation.AnimatorSet().apply {
            playTogether(translateYFront, translateYBack, tiltFront, tiltBack)
            start()
        }
    }

    private fun performLinkCard() {
        val fetchedCardNumber = com.pnb.bank.data.api.ApiConstants.activeCardNumber
        val displayName = if (selectedNameFormat.isNotEmpty()) selectedNameFormat else savedCustomerName
        val variantCode = selectedCardVariant
            .trim()
            .uppercase()
            .replace("\\s+".toRegex(), "_")

        // 1. Immediately switch UI to Loading screen on Proceed click
        binding.cardConfirmationStep.visibility = View.GONE
        binding.cardSuccessStep.visibility = View.VISIBLE
        binding.btnProceedContainer.visibility = View.GONE
        binding.btnBackContainer.visibility = View.GONE
        binding.btnHomeContainer.visibility = View.GONE

        // Stop card animators to free system memory
        cardAnimatorSet?.cancel()
        cardAnimatorSet = null

        // Show spinner & initial clean status text
        binding.pbCardSuccess.visibility = View.VISIBLE
        binding.tvSuccessTitle.text = "Initiating Your Debit Card"
        binding.tvSuccessDetails.text = "Please wait while we process card re-issuance..."
        binding.tvCountdownText.visibility = View.GONE

        val startTime = System.currentTimeMillis()

        lifecycleScope.launch {
            var attempt = 1
            var isSuccess = false
            var lastResponse: com.pnb.bank.data.api.models.LinkCardResponse? = null
            var lastErrorMsg = ""

            // 2. Perform API call with up to 3 Retries on Failure
            while (attempt <= 3 && !isSuccess) {
                AppLogger.i("API 3 [linkCard] Attempt #$attempt of 3...")

                val result = apiRepository.linkCard(
                    accountNumber = if (savedAccountNumber.isNotEmpty()) savedAccountNumber else "666444222",
                    cardNumber = fetchedCardNumber,
                    cardVariant = variantCode,
                    customerName = if (savedCustomerName.isNotEmpty()) savedCustomerName else "Nikhil Randive"
                )

                when (result) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        val apiOk = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"
                        if (apiOk) {
                            isSuccess = true
                            lastResponse = response
                            AppLogger.i("API 3 Success [linkCard] on attempt #$attempt: Status=${response.status} | CardStatus=${response.cardStatus}")
                        } else {
                            lastErrorMsg = response.cardStatus ?: response.responseMessage ?: "Card link failed"
                            AppLogger.w("API 3 Response Error [linkCard] attempt #$attempt: $lastErrorMsg")
                            attempt++
                        }
                    }
                    is NetworkResult.Error -> {
                        lastErrorMsg = result.message
                        AppLogger.w("API 3 Network Error [linkCard] attempt #$attempt: $lastErrorMsg")
                        attempt++
                    }
                    is NetworkResult.Loading -> {}
                }

                if (!isSuccess && attempt <= 3) {
                    kotlinx.coroutines.delay(1000) // Wait 1 second before retry
                }
            }

            // 3. Enforce Minimum 4 Seconds Loading Screen
            val elapsedTime = System.currentTimeMillis() - startTime
            val minLoadingMs = 4000L
            if (elapsedTime < minLoadingMs) {
                kotlinx.coroutines.delay(minLoadingMs - elapsedTime)
            }

            // 4. Construct Result JSON Payload for Parent App
            val resultJsonObject = org.json.JSONObject().apply {
                put("status", if (isSuccess) "SUCCESS" else "FAILED")
                put("accountNumber", if (savedAccountNumber.isNotEmpty()) savedAccountNumber else "666444222")
                put("cardNumber", fetchedCardNumber)
                put("cardVariant", variantCode)
                put("customerName", if (savedCustomerName.isNotEmpty()) savedCustomerName else "Nikhil Randive")
                put("nameOnCard", displayName)
                put("cardStatus", if (isSuccess) (lastResponse?.cardStatus ?: "ACTIVE") else "FAILED")
                put("responseCode", if (isSuccess) (lastResponse?.responseCode ?: "00") else "99")
                put("attempts", if (isSuccess) (attempt) else 3)
                if (!isSuccess) put("errorMessage", lastErrorMsg)
            }
            val resultJsonString = resultJsonObject.toString()

            AppLogger.i("=================================================================")
            AppLogger.i("RESULT HANDOVER TO PARENT APP:")
            AppLogger.i("INTENT EXTRA KEY : ${com.pnb.bank.utils.AppConstants.KEY_RESULT_DATA}")
            AppLogger.i("JSON PAYLOAD     : $resultJsonString")
            AppLogger.i("=================================================================")
            
            // Print under multiple tags so it shows in all Logcat filters ([API_CLIENT], PNB_KIOSK, PNB_RESULT_HANDOVER)
            android.util.Log.e("PNB_KIOSK", "=================================================================")
            android.util.Log.e("PNB_KIOSK", "[API_CLIENT] RESULT HANDOVER JSON (Key: ${com.pnb.bank.utils.AppConstants.KEY_RESULT_DATA}):")
            android.util.Log.e("PNB_KIOSK", "[API_CLIENT] $resultJsonString")
            android.util.Log.e("PNB_KIOSK", "=================================================================")
            android.util.Log.d("PNB_RESULT_HANDOVER", "KEY: ${com.pnb.bank.utils.AppConstants.KEY_RESULT_DATA} | PAYLOAD: $resultJsonString")
            println("PNB_HANDOVER_JSON: $resultJsonString")

            // 5. Send Result Intent back to Parent App safely
            val resultIntent = android.content.Intent().apply {
                putExtra(com.pnb.bank.utils.AppConstants.KEY_RESULT_DATA, resultJsonString)
                putExtra("status", if (isSuccess) "SUCCESS" else "FAILED")
            }
            setResult(RESULT_OK, resultIntent)

            // 6. Finish activity cleanly and return to parent app
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cardAnimatorSet?.cancel()
        cardAnimatorSet = null
    }
}
