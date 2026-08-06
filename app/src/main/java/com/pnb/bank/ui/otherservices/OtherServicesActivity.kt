package com.pnb.bank.ui.otherservices

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pnb.bank.R
import com.pnb.bank.databinding.ActivityOtherServicesBinding
import com.pnb.bank.databinding.ItemServiceCardBinding

class OtherServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherServicesBinding
    private var categoriesList: List<ServiceCategory> = emptyList()
    private var currentCategory: ServiceCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.glassInnerContainer.outlineSpotShadowColor = android.graphics.Color.parseColor("#B0000000")
            binding.glassInnerContainer.outlineAmbientShadowColor = android.graphics.Color.parseColor("#50000000")
        }

        loadServicesData()
        setupCategoriesGrid()
        setupListeners()
    }

    private fun loadServicesData() {
        categoriesList = OtherServicesRepository.loadCategoriesFromJson(this)
    }

    private fun setupCategoriesGrid() {
        binding.gridCategories.removeAllViews()

        val inflater = LayoutInflater.from(this)
        categoriesList.forEach { category ->
            val cardView = inflater.inflate(R.layout.item_service_card, binding.gridCategories, false) as LinearLayout
            
            // Prominent large size for 5 category cards in 1 row
            val params = LinearLayout.LayoutParams(dpToPx(250), dpToPx(230))
            params.setMargins(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            cardView.layoutParams = params
            cardView.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))

            val tvIcon = cardView.findViewById<TextView>(R.id.tvCardIcon)
            val tvTitle = cardView.findViewById<TextView>(R.id.tvCardTitle)

            tvIcon.text = category.iconEmoji
            tvIcon.textSize = 42f
            tvTitle.text = category.title
            tvTitle.textSize = 19f

            cardView.setOnClickListener {
                if (category.services.size == 1) {
                    currentCategory = category
                    openServiceWebView(category.services.first())
                } else {
                    showServicesListScreen(category)
                }
            }

            binding.gridCategories.addView(cardView)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun showServicesListScreen(category: ServiceCategory) {
        currentCategory = category
        binding.tvCategoryTitle.text = category.title
        binding.tvCategorySubtitle.text = "Select a service to launch digital application portal"

        binding.gridServices.removeAllViews()
        val inflater = LayoutInflater.from(this)

        category.services.forEach { service ->
            val bindingItem = ItemServiceCardBinding.inflate(inflater, binding.gridServices, false)
            
            val params = LinearLayout.LayoutParams(dpToPx(210), dpToPx(190))
            params.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            bindingItem.root.layoutParams = params
            bindingItem.root.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            bindingItem.root.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_white_service_card)

            bindingItem.tvCardIcon.text = service.iconEmoji
            bindingItem.tvCardIcon.textSize = 38f

            bindingItem.tvCardTitle.text = service.title
            bindingItem.tvCardTitle.setTextColor(android.graphics.Color.parseColor("#2D3748"))
            bindingItem.tvCardTitle.textSize = 17f

            bindingItem.root.setOnClickListener {
                openServiceWebView(service)
            }

            binding.gridServices.addView(bindingItem.root)
        }

        binding.layoutCategoriesScreen.visibility = View.GONE
        binding.layoutServicesListScreen.visibility = View.VISIBLE

        binding.btnBackContainer.visibility = View.VISIBLE
    }

    private fun openServiceWebView(service: ServiceItem) {
        val intent = Intent(this, ServiceWebViewActivity::class.java).apply {
            putExtra(ServiceWebViewActivity.EXTRA_TITLE, "${service.iconEmoji} ${service.title}")
            putExtra(ServiceWebViewActivity.EXTRA_URL, service.url)
        }
        startActivity(intent)
    }

    private fun setupListeners() {
        binding.btnHomeContainer.setOnClickListener {
            finishAffinity()
        }

        binding.btnBackContainer.setOnClickListener {
            handleBackNavigation()
        }
    }

    private fun showCategoriesScreen() {
        binding.layoutCategoriesScreen.visibility = View.VISIBLE
        binding.layoutServicesListScreen.visibility = View.GONE

        binding.btnBackContainer.visibility = View.GONE
    }

    private fun handleBackNavigation() {
        if (binding.layoutServicesListScreen.visibility == View.VISIBLE) {
            showCategoriesScreen()
        } else {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBackNavigation()
    }
}
