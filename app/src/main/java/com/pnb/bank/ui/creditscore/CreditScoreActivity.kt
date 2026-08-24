package com.pnb.bank.ui.creditscore

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pnb.bank.R
import com.pnb.bank.data.api.ApiRepository
import com.pnb.bank.data.api.NetworkResult
import com.pnb.bank.databinding.ActivityCreditScoreBinding
import com.pnb.bank.utils.AppConstants
import com.pnb.bank.utils.hideSystemUI
import kotlinx.coroutines.launch

class CreditScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditScoreBinding
    private var activeEditText: EditText? = null
    private lateinit var apiRepository: ApiRepository
    private var progressDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        binding = ActivityCreditScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiRepository = ApiRepository()

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
                    switchKeyboardForFocus()
                }
            }

            editText.setOnClickListener {
                activeEditText = editText
                switchKeyboardForFocus()
            }
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

    private fun setupListeners() {
        // Back navigation
        binding.btnBackContainer.setOnClickListener {
            finish()
        }

        // Submit Button click
        binding.btnSubmitCreditScore.setOnClickListener {
            validateAndSubmit()
        }

        // Test Data click
        binding.btnTestData.setOnClickListener {
            showTestAccountSelectionDialog()
        }
    }

    private fun showTestAccountSelectionDialog() {
        val list = AppConstants.TEST_CREDIT_SCORE_LIST
        val items = list.map { "${it.name} (${it.mobile})" }.toTypedArray()

        val contextWrapper = android.view.ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
        androidx.appcompat.app.AlertDialog.Builder(contextWrapper)
            .setTitle("Select Test Profile")
            .setItems(items) { dialog, which ->
                val selected = list[which]
                binding.etFullName.setText(selected.name)
                binding.etMobileNumber.setText(selected.mobile)
                binding.etDocumentId.setText(selected.documentId)
                dialog.dismiss()
                Toast.makeText(this, "Autofilled with: ${selected.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showProgressDialog(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val padding = 36
        val linearLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(padding, padding, padding, padding)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val progressBar = android.widget.ProgressBar(this).apply {
            isIndeterminate = true
        }
        val textView = android.widget.TextView(this).apply {
            text = message
            textSize = 18f
            setTextColor(Color.BLACK)
            setPadding(padding, 0, 0, 0)
        }
        linearLayout.addView(progressBar)
        linearLayout.addView(textView)
        builder.setView(linearLayout)
        builder.setCancelable(false)
        progressDialog = builder.create().apply {
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
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

        // Trigger Button Loading State
        binding.tvSubmitText.visibility = View.GONE
        binding.pbSubmitLoading.visibility = View.VISIBLE
        binding.btnSubmitCreditScore.isEnabled = false
        binding.btnSubmitCreditScore.alpha = 0.6f

        // Trigger API Call
        lifecycleScope.launch {
            val result = apiRepository.fetchCreditScoreUnified(
                name = name,
                mobileNumber = mobile,
                panNumber = documentId
            )
            
            // Restore Button Normal State
            binding.tvSubmitText.visibility = View.VISIBLE
            binding.pbSubmitLoading.visibility = View.GONE
            binding.btnSubmitCreditScore.isEnabled = true
            binding.btnSubmitCreditScore.alpha = 1.0f

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val score = response.data?.ccrResponse?.cirReportDataList?.firstOrNull()
                        ?.cirReportData?.scoreDetails?.firstOrNull()?.value

                    if (!score.isNullOrEmpty()) {
                        val intent = Intent(this@CreditScoreActivity, CreditScoreResultActivity::class.java).apply {
                            putExtra("credit_response_json", com.google.gson.Gson().toJson(response))
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val message = response.message ?: "Credit report fetched but no score details found."
                        showError(message)
                    }
                }
                is NetworkResult.Error -> {
                    showError(result.message ?: "Failed to connect to gateway. Please try again.")
                }
                is NetworkResult.Loading -> {
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
