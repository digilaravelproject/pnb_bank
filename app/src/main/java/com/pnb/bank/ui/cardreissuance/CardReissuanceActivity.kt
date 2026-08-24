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
import com.pnb.bank.utils.AppConstants
import com.pnb.bank.utils.AppLogger
import com.pnb.bank.utils.hideSystemUI
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
    private var savedMobileNumber: String = ""
    private var savedCustomerName: String = ""
    private var savedEligibleVariants: List<String> = emptyList()

    private val apiRepository by lazy { ApiRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
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

        // Configure Step 1 UI dynamically based on AppConstants.IS_PAN_MODE_ENABLED
        setupModeConfiguration()

        // Call Bank AccessTokenService API on app open to fetch & cache OAuth Token immediately
        fetchBankOAuthTokenOnAppOpen()
    }

    private fun setupModeConfiguration() {
        if (com.pnb.bank.utils.AppConstants.IS_PAN_MODE_ENABLED) {
            // PAN Mode (Default)
            binding.tvStep1Subtitle.text = "Enter Customer Account Number and PAN for CBS Validation"
            binding.tvPanLabel.text = "PAN Number"
            binding.etPanNumber.hint = "Enter 10-digit PAN (Optional / Required)"
            binding.etPanNumber.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            binding.etPanNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            binding.btnAutoFillTest.text = "⚡ Auto-Fill Test Data (Select Account & PAN)"
        } else {
            // Mobile Mode
            binding.tvStep1Subtitle.text = "Enter Customer Account Number and Mobile Number for Validation"
            binding.tvPanLabel.text = "Mobile Number"
            binding.etPanNumber.hint = "Enter Mobile Number"
            binding.etPanNumber.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            binding.etPanNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(13))
            binding.btnAutoFillTest.text = "⚡ Auto-Fill Test Data (Select Account & Mobile)"
        }
    }




    private var tokenRefreshHandler: android.os.Handler? = null
    private var tokenRefreshRunnable: Runnable? = null

    private fun fetchBankOAuthTokenOnAppOpen() {
        lifecycleScope.launch {
            AppLogger.i("App Opened: Fetching Bank OAuth Access Token via AccessTokenService...")
            val result = apiRepository.getBankAccessToken()
            when (result) {
                is NetworkResult.Success -> {
                    val expiresInSeconds = result.data.expiresIn ?: com.pnb.bank.data.api.ApiConstants.tokenExpiresInSeconds
                    AppLogger.i("App Open Bank OAuth Token Success (expires_in: ${expiresInSeconds}s): ${result.data.accessToken?.take(15)}...")

                    // Schedule automatic background refresh 60 seconds before token expires
                    scheduleTokenAutoRefresh(expiresInSeconds)
                }
                is NetworkResult.Error -> {
                    AppLogger.w("App Open Bank OAuth Token Error: ${result.message}")
                    // Retry after 10 seconds if fetch failed
                    scheduleTokenAutoRefresh(10)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun scheduleTokenAutoRefresh(expiresInSeconds: Long) {
        tokenRefreshHandler?.removeCallbacks(tokenRefreshRunnable ?: return)

        tokenRefreshHandler = android.os.Handler(android.os.Looper.getMainLooper())
        tokenRefreshRunnable = Runnable {
            AppLogger.i("Token Expiry Timer Fired: Refreshing Bank OAuth Access Token automatically...")
            lifecycleScope.launch {
                apiRepository.getBankAccessToken(forceRefresh = true)
            }
        }

        // Refresh 60 seconds before actual expiration (minimum 5 seconds delay)
        val refreshDelayMs = ((expiresInSeconds - 60).coerceAtLeast(5)) * 1000L
        tokenRefreshHandler?.postDelayed(tokenRefreshRunnable!!, refreshDelayMs)
        AppLogger.i("Scheduled next Bank OAuth Token auto-refresh in ${refreshDelayMs / 1000} seconds")
    }






    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
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

            // REAL-TIME UPDATE of name on Card Preview (Right Column)
            binding.tvCardMockupName.text = selectedNameFormat.uppercase()
        }

        // Setup 3D Card Flip Listener on Tap
        val flipListener = View.OnClickListener {
            flipCardMockup()
        }
        binding.flCardFlipContainer.setOnClickListener(flipListener)
        binding.btnFlipCard.setOnClickListener(flipListener)
    }

    private var isCardShowingFront = true
    private fun flipCardMockup() {
        val frontView = binding.rlDebitCardMockup
        val backView = binding.rlDebitCardMockupBack

        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        frontView.cameraDistance = scale
        backView.cameraDistance = scale

        if (isCardShowingFront) {
            // Flip to BACK
            val flipOutFront = android.animation.ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f).apply { duration = 250 }
            val flipInBack = android.animation.ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f).apply { duration = 250 }
            flipOutFront.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    frontView.visibility = View.GONE
                    backView.visibility = View.VISIBLE
                    flipInBack.start()
                }
            })
            flipOutFront.start()
            isCardShowingFront = false
            binding.btnFlipCard.text = "🔄 Tap to Flip Card"
        } else {
            // Flip to FRONT
            val flipOutBack = android.animation.ObjectAnimator.ofFloat(backView, "rotationY", 0f, 90f).apply { duration = 250 }
            val flipInFront = android.animation.ObjectAnimator.ofFloat(frontView, "rotationY", -90f, 0f).apply { duration = 250 }
            flipOutBack.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    backView.visibility = View.GONE
                    frontView.visibility = View.VISIBLE
                    flipInFront.start()
                }
            })
            flipOutBack.start()
            isCardShowingFront = true
            binding.btnFlipCard.text = "🔄 Tap to Flip Card"
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

                val maxLength = when (target.id) {
                    binding.etAccountNumber.id -> 16
                    binding.etPanNumber.id -> if (com.pnb.bank.utils.AppConstants.IS_PAN_MODE_ENABLED) 10 else 13
                    else -> 6
                }
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
            binding.etOtpNumber.setText("")
            binding.tvOtpError.visibility = View.GONE

            if (savedMobileNumber.isNotEmpty()) {
                Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
                setProceedLoading(true, "Resending OTP...")

                lifecycleScope.launch {
                    val result = apiRepository.generateOtp(mobileNumber = savedMobileNumber)
                    setProceedLoading(false)

                    when (result) {
                        is NetworkResult.Success -> {
                            val response = result.data
                            val isSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || (response.otpSent == true)
                            if (isSuccess) {
                                Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Resent Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                binding.tvOtpError.text = response.responseMessage ?: "Failed to resend OTP"
                                binding.tvOtpError.visibility = View.VISIBLE
                                Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "Failed to resend OTP", Toast.LENGTH_LONG).show()
                            }
                        }
                        is NetworkResult.Error -> {
                            binding.tvOtpError.text = result.message
                            binding.tvOtpError.visibility = View.VISIBLE
                            Toast.makeText(this@CardReissuanceActivity, "Resend OTP Failed: ${result.message}", Toast.LENGTH_LONG).show()
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } else {
                val accountNo = if (savedAccountNumber.isNotEmpty()) savedAccountNumber else binding.etAccountNumber.text.toString().trim()
                val panNo = binding.etPanNumber.text.toString().trim()
                if (accountNo.isNotEmpty()) {
                    performCbsValidation(accountNo, panNo)
                }
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
                // Step 4: Name Variant Selected -> Directly execute Link Card API (pg/api/v1/debitcard/linkCard)
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
    }

    private fun showTestAccountSelectionDialog() {
        val isPanMode = com.pnb.bank.utils.AppConstants.IS_PAN_MODE_ENABLED
        val contextWrapper = android.view.ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)

        if (isPanMode) {
            val panAccounts = com.pnb.bank.utils.AppConstants.TEST_PAN_ACCOUNTS_LIST
            val options = panAccounts.map { "Acc: ${it.accountNumber}   |   PAN: ${it.panNumber}" }.toTypedArray()

            androidx.appcompat.app.AlertDialog.Builder(contextWrapper)
                .setTitle("Select Test Account (PAN Mode)")
                .setItems(options) { _, which ->
                    val selected = panAccounts[which]
                    binding.etAccountNumber.setText(selected.accountNumber)
                    binding.etPanNumber.setText(selected.panNumber)
                    binding.tvAccountError.visibility = View.GONE
                    binding.tvPanError.visibility = View.GONE
                    binding.btnAutoFillTest.text = "⚡ Auto-Fill (${selected.accountNumber} / ${selected.panNumber})"
                    Toast.makeText(this, "Selected: Acc ${selected.accountNumber}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            val mobileAccounts = com.pnb.bank.utils.AppConstants.TEST_MOBILE_ACCOUNTS_LIST
            val options = mobileAccounts.map { "Acc: ${it.accountNumber}   |   Mobile: ${it.mobileNumber}" }.toTypedArray()

            androidx.appcompat.app.AlertDialog.Builder(contextWrapper)
                .setTitle("Select Test Account (Mobile Mode)")
                .setItems(options) { _, which ->
                    val selected = mobileAccounts[which]
                    binding.etAccountNumber.setText(selected.accountNumber)
                    binding.etPanNumber.setText(selected.mobileNumber)
                    binding.tvAccountError.visibility = View.GONE
                    binding.tvPanError.visibility = View.GONE
                    binding.btnAutoFillTest.text = "⚡ Auto-Fill (${selected.accountNumber} / ${selected.mobileNumber})"
                    Toast.makeText(this, "Selected: Acc ${selected.accountNumber}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }





    private fun handleCardReissuanceBackNavigation() {
        if (binding.cardNameSelectionStep.visibility == View.VISIBLE) {
            // Bypass Card Selection Step on back navigation
            binding.cardNameSelectionStep.visibility = View.GONE
            binding.cardOtpStep.visibility = View.VISIBLE
            binding.cardKeypadContainer.visibility = View.VISIBLE
            activeEditText = binding.etOtpNumber
            binding.etOtpNumber.requestFocus()
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

    private fun performCbsValidation(accountNo: String, inputSecondVal: String) {
        setProceedLoading(true, "Validating...")
        binding.tvAccountError.visibility = View.GONE
        binding.tvPanError.visibility = View.GONE
        savedAccountNumber = accountNo

        lifecycleScope.launch {
            if (com.pnb.bank.utils.AppConstants.IS_PAN_MODE_ENABLED) {
                // 1. PAN MODE: Standard validateCustomer API
                val result = apiRepository.validateCustomer(
                    panNumber = inputSecondVal.ifEmpty { "ABCDE1234F" },
                    accountNumber = accountNo
                )

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

                            val mobileNum = response.customerMobileNumber?.trim()
                            if (!mobileNum.isNullOrEmpty() && !mobileNum.equals("null", ignoreCase = true)) {
                                savedMobileNumber = mobileNum
                            }

                            // Extract Customer Name from API 1 if present
                            val nameParts = listOfNotNull(response.firstName, response.middleName, response.lastName)
                                .map { it.trim() }
                                .filter { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

                            if (nameParts.isNotEmpty()) {
                                savedCustomerName = nameParts.joinToString(" ")
                                AppLogger.i("API 1 Customer Name Extracted: $savedCustomerName")
                            }

                            AppLogger.i("API 1 Success [validateCustomer]: TxnId=$savedTransactionId | Mobile=$savedMobileNumber")

                            // NOW CALL API 2: generateOtp
                            triggerGenerateOtpFlow()
                        } else {
                            setProceedLoading(false)
                            AppLogger.w("API 1 Response Error [validateCustomer]: ${response.responseMessage}")
                            binding.tvPanError.text = response.responseMessage ?: "Validation Failed"
                            binding.tvPanError.visibility = View.VISIBLE
                            Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "Validation Failed", Toast.LENGTH_LONG).show()
                        }
                    }
                    is NetworkResult.Error -> {
                        setProceedLoading(false)
                        AppLogger.w("API 1 Network Error [validateCustomer]: ${result.message}")
                        binding.tvPanError.text = result.message
                        binding.tvPanError.visibility = View.VISIBLE
                        Toast.makeText(this@CardReissuanceActivity, "Validation Failed: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                    is NetworkResult.Loading -> {}
                }
            } else {
                // 2. MOBILE MODE: Bank Gateway CustomerDetails (Encrypted / Plain depending on IS_ENCRYPTION_ENABLED)
                val rawMobile = if (inputSecondVal.length >= 10) inputSecondVal else "7757011027"
                val mobileInput = if (rawMobile.length == 10 && !rawMobile.startsWith("91")) "91$rawMobile" else rawMobile

                val result = apiRepository.fetchCustomerDetailsUnified(
                    accountNumber = accountNo,
                    mobileNumber = mobileInput
                )


                when (result) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        val isApiSuccess = response.status?.equals("Success", ignoreCase = true) == true && response.resultData != null

                        if (isApiSuccess) {
                            val details = response.resultData
                            val fetchedMobile = details?.mobileNo?.trim()

                            // Check IS_DYNAMIC_MOBILE_ENABLED toggle flag:
                            // true  -> Use Dynamic Mobile Number fetched from API (fetchedMobile)
                            // false -> Use Test Mobile Number (DEFAULT_OTP_MOBILE_NUMBER = "917458086472")
                            savedMobileNumber = if (com.pnb.bank.utils.AppConstants.IS_DYNAMIC_MOBILE_ENABLED) {
                                if (!fetchedMobile.isNullOrEmpty()) fetchedMobile else mobileInput
                            } else {
                                com.pnb.bank.utils.AppConstants.DEFAULT_OTP_MOBILE_NUMBER
                            }

                            AppLogger.i("Mobile Selection Mode [IS_DYNAMIC_MOBILE_ENABLED = ${com.pnb.bank.utils.AppConstants.IS_DYNAMIC_MOBILE_ENABLED}]: Fetched from API = '$fetchedMobile' | Selected for OTP = '$savedMobileNumber'")


                            val nameParts = listOfNotNull(details?.firstName, details?.middleName, details?.lastName)
                                .map { it.trim() }
                                .filter { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }


                            if (nameParts.isNotEmpty()) {
                                savedCustomerName = nameParts.joinToString(" ")
                                AppLogger.i("CustomerDetails Name Extracted: $savedCustomerName")
                            }

                            AppLogger.i("API 1 Success [fetchCustomerDetailsUnified]: Mobile=$savedMobileNumber | Name=$savedCustomerName")

                            // NOW CALL API 2: generateOtp with fetched mobile number
                            triggerGenerateOtpFlow()
                        } else {
                            setProceedLoading(false)
                            val errorMsg = response.remarks ?: "NO DATA FOUND"
                            AppLogger.w("API 1 Response Error [fetchCustomerDetailsUnified]: $errorMsg")
                            binding.tvPanError.text = errorMsg
                            binding.tvPanError.visibility = View.VISIBLE
                            Toast.makeText(this@CardReissuanceActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                    is NetworkResult.Error -> {
                        setProceedLoading(false)
                        AppLogger.w("API 1 Network Error [fetchCustomerDetailsUnified]: ${result.message}")
                        binding.tvPanError.text = result.message
                        binding.tvPanError.visibility = View.VISIBLE
                        Toast.makeText(this@CardReissuanceActivity, "Customer Details Failed: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    private suspend fun triggerGenerateOtpFlow() {
        setProceedLoading(true, "Generating OTP...")
        val otpResult = apiRepository.generateOtp(mobileNumber = savedMobileNumber)
        setProceedLoading(false)

        when (otpResult) {
            is NetworkResult.Success -> {
                val otpResp = otpResult.data
                val isOtpSuccess = (otpResp.status?.equals("SUCCESS", ignoreCase = true) == true) || (otpResp.otpSent == true)

                if (isOtpSuccess) {
                    // Mask mobile for UI display
                    val maskedMobile = if (savedMobileNumber.length >= 4) {
                        "*".repeat(savedMobileNumber.length - 4) + savedMobileNumber.takeLast(4)
                    } else {
                        "********" + savedMobileNumber
                    }
                    binding.tvOtpSubtitle.text = "OTP has been sent to your registered mobile number $maskedMobile"

                    // BOTH APIS SUCCESS -> NAVIGATE TO OTP SCREEN
                    binding.cardAccountStep.visibility = View.GONE
                    binding.cardOtpStep.visibility = View.VISIBLE
                    binding.btnHomeContainer.visibility = View.GONE
                    binding.btnBackContainer.visibility = View.VISIBLE
                    binding.etOtpNumber.setText("")
                    binding.tvOtpError.visibility = View.GONE
                    activeEditText = binding.etOtpNumber
                    binding.etOtpNumber.requestFocus()

                    Toast.makeText(this@CardReissuanceActivity, otpResp.responseMessage ?: "OTP Generated Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    AppLogger.w("API generateOtp Error: ${otpResp.responseMessage}")
                    binding.tvPanError.text = otpResp.responseMessage ?: "Failed to generate OTP"
                    binding.tvPanError.visibility = View.VISIBLE
                    Toast.makeText(this@CardReissuanceActivity, otpResp.responseMessage ?: "Failed to generate OTP", Toast.LENGTH_LONG).show()
                }
            }
            is NetworkResult.Error -> {
                AppLogger.w("API generateOtp Network Error: ${otpResult.message}")
                binding.tvPanError.text = otpResult.message
                binding.tvPanError.visibility = View.VISIBLE
                Toast.makeText(this@CardReissuanceActivity, "Generate OTP Failed: ${otpResult.message}", Toast.LENGTH_LONG).show()
            }
            is NetworkResult.Loading -> {}
        }
    }




    private fun performOtpVerification(otp: String) {
        setProceedLoading(true, "Verifying...")
        binding.tvOtpError.visibility = View.GONE

        lifecycleScope.launch {
            val result = apiRepository.verifyOtp(
                mobileNumber = savedMobileNumber,
                otp = otp
            )

            setProceedLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val isApiSuccess = (response.status?.equals("SUCCESS", ignoreCase = true) == true) || response.responseCode == "00"

                    if (isApiSuccess) {
                        // Customer Name is extracted from API 1 (validateCustomer: firstName, middleName, lastName)
                        val finalCustomerName = savedCustomerName.ifEmpty { "Customer" }
                        
                        savedEligibleVariants = listOf(AppConstants.DEFAULT_CARD_VARIANT)
                        selectedCardVariant = AppConstants.DEFAULT_CARD_VARIANT

                        AppLogger.i("API Success [verifyOtp]: CustomerName=$finalCustomerName | Status=${response.status}")

                        updateCustomerNameOptions(finalCustomerName)

                        // SKIP CARD SELECTION SCREEN & NAVIGATE DIRECTLY TO NAME SELECTION SCREEN
                        binding.cardOtpStep.visibility = View.GONE
                        binding.cardKeypadContainer.visibility = View.GONE
                        binding.cardSelectionStep.visibility = View.GONE
                        binding.cardNameSelectionStep.visibility = View.VISIBLE

                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        AppLogger.w("API Response Error [verifyOtp]: ${response.responseMessage}")
                        binding.tvOtpError.text = response.responseMessage ?: "OTP Verification Failed"
                        binding.tvOtpError.visibility = View.VISIBLE
                        Toast.makeText(this@CardReissuanceActivity, response.responseMessage ?: "OTP Verification Failed", Toast.LENGTH_LONG).show()
                    }
                }
                is NetworkResult.Error -> {
                    AppLogger.w("API Network Error [verifyOtp]: ${result.message}")

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

        // Initialize Card Preview Data in Right Column
        val fetchedCardNumber = com.pnb.bank.data.api.ApiConstants.activeCardNumber
        binding.tvCardMockupName.text = selectedNameFormat.uppercase()
        binding.tvCardMockupVariant.text = if (selectedCardVariant.contains("SELECT", ignoreCase = true)) "Select" else selectedCardVariant.uppercase()
        val formattedCardNum = if (fetchedCardNumber.length >= 4) {
            "XXXX  XXXX  XXXX  ${fetchedCardNumber.takeLast(4)}"
        } else {
            "XXXX  XXXX  XXXX  8888"
        }
        binding.tvCardMockupNumber.text = formattedCardNum
        binding.tvCardBackMockupNumber.text = formattedCardNum
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
        binding.cardNameSelectionStep.visibility = View.GONE
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
            var lastResponse: com.pnb.bank.data.api.debitcard.models.LinkCardResponse? = null

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
        tokenRefreshHandler?.removeCallbacks(tokenRefreshRunnable ?: return)
        cardAnimatorSet?.cancel()
        cardAnimatorSet = null
    }

}
