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
import com.pnb.bank.utils.hideSystemUI

class OtherServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherServicesBinding
    private var categoriesList: List<ServiceCategory> = emptyList()
    private var currentCategory: ServiceCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun loadServicesData() {
        categoriesList = OtherServicesRepository.loadCategoriesFromJson(this)
    }

    private fun setupCategoriesGrid() {
        binding.gridCategories.removeAllViews()

        val displayMetrics = resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels

        val screenWidthDp = screenWidthPx / displayMetrics.density

        // Dynamic column count based on screen width
        val catCols = when {
            screenWidthDp >= 1350 -> 7
            screenWidthDp >= 1000 -> 6
            screenWidthDp >= 750 -> 5
            else -> 4
        }
        binding.gridCategories.columnCount = catCols

        val totalItems = categoriesList.size
        val catRows = Math.ceil(totalItems.toDouble() / catCols).toInt().coerceAtLeast(1)

        val marginHorizPx = dpToPx((140 / catCols).coerceIn(6, 12))
        val marginVertPx = dpToPx((140 / catCols).coerceIn(6, 12))

        val availWidth = screenWidthPx - dpToPx(170)
        val availHeight = screenHeightPx - dpToPx(210)

        val widthBased = (availWidth / catCols) - (marginHorizPx * 2)
        val heightBased = (availHeight / catRows) - (marginVertPx * 2)

        // Dynamic card size scaling: larger on big screens, smaller on small screens
        val cardWidthPx = minOf(widthBased, (heightBased / 0.82f).toInt()).coerceIn(dpToPx(100), dpToPx(250))
        val cardHeightPx = minOf(heightBased, (cardWidthPx * 0.86f).toInt()).coerceIn(dpToPx(90), dpToPx(220))

        val iconTextSizeSp = (cardHeightPx * 0.22f / displayMetrics.density).coerceIn(18f, 38f)
        val titleTextSizeSp = (cardHeightPx * 0.10f / displayMetrics.density).coerceIn(10f, 17f)

        val inflater = LayoutInflater.from(this)
        categoriesList.forEach { category ->
            val cardView = inflater.inflate(R.layout.item_service_card, binding.gridCategories, false) as LinearLayout
            
            val params = LinearLayout.LayoutParams(cardWidthPx, cardHeightPx)
            params.setMargins(marginHorizPx, marginVertPx, marginHorizPx, marginVertPx)
            cardView.layoutParams = params
            cardView.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))

            val tvIcon = cardView.findViewById<TextView>(R.id.tvCardIcon)
            val tvTitle = cardView.findViewById<TextView>(R.id.tvCardTitle)

            tvIcon.text = category.iconEmoji
            tvIcon.textSize = iconTextSizeSp
            tvTitle.text = category.title
            tvTitle.textSize = titleTextSizeSp

            cardView.setOnClickListener {
                when (category.targetType) {
                    "SUB_LIST" -> {
                        showServicesListScreen(category)
                    }
                    "NATIVE_ACTIVITY" -> {
                        launchNativeActivity(category.targetClass, category.title)
                    }
                    "WEBVIEW" -> {
                        if (category.url.isNotEmpty()) {
                            openServiceWebView(ServiceItem(category.title, category.iconEmoji, category.url))
                        } else {
                            android.widget.Toast.makeText(this, "${category.title} service is coming soon", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        if (category.services.isNotEmpty()) {
                            showServicesListScreen(category)
                        } else if (category.url.isNotEmpty()) {
                            openServiceWebView(ServiceItem(category.title, category.iconEmoji, category.url))
                        } else {
                            android.widget.Toast.makeText(this, "${category.title} service is coming soon", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
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

        val displayMetrics = resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels

        val screenWidthDp = screenWidthPx / displayMetrics.density
        val totalServices = category.services.size

        // Dynamic column count for services grid
        val servCols = when {
            screenWidthDp >= 1350 && totalServices > 12 -> 7
            screenWidthDp >= 1050 && totalServices > 8 -> 6
            screenWidthDp >= 750 -> 5
            else -> 4
        }.coerceAtMost(totalServices.coerceAtLeast(1))

        binding.gridServices.columnCount = servCols

        val servRows = Math.ceil(totalServices.toDouble() / servCols).toInt().coerceAtLeast(1)

        val marginHorizPx = dpToPx((120 / servCols).coerceIn(5, 10))
        val marginVertPx = dpToPx((120 / servCols).coerceIn(5, 10))

        val availWidth = screenWidthPx - dpToPx(170)
        val availHeight = screenHeightPx - dpToPx(210)

        val widthBased = (availWidth / servCols) - (marginHorizPx * 2)
        val heightBased = (availHeight / servRows) - (marginVertPx * 2)

        // Dynamic card size scaling: larger on big screens, smaller on small screens
        val cardWidthPx = minOf(widthBased, (heightBased / 0.80f).toInt()).coerceIn(dpToPx(85), dpToPx(240))
        val cardHeightPx = minOf(heightBased, (cardWidthPx * 0.84f).toInt()).coerceIn(dpToPx(75), dpToPx(210))

        val iconTextSizeSp = (cardHeightPx * 0.22f / displayMetrics.density).coerceIn(15f, 36f)
        val titleTextSizeSp = (cardHeightPx * 0.095f / displayMetrics.density).coerceIn(9f, 16f)

        val inflater = LayoutInflater.from(this)

        category.services.forEach { service ->
            val bindingItem = ItemServiceCardBinding.inflate(inflater, binding.gridServices, false)
            
            val params = LinearLayout.LayoutParams(cardWidthPx, cardHeightPx)
            params.setMargins(marginHorizPx, marginVertPx, marginHorizPx, marginVertPx)
            bindingItem.root.layoutParams = params
            bindingItem.root.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            bindingItem.root.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_white_service_card)

            bindingItem.tvCardIcon.text = service.iconEmoji
            bindingItem.tvCardIcon.textSize = iconTextSizeSp

            bindingItem.tvCardTitle.text = service.title
            bindingItem.tvCardTitle.setTextColor(android.graphics.Color.parseColor("#2D3748"))
            bindingItem.tvCardTitle.textSize = titleTextSizeSp

            bindingItem.root.setOnClickListener {
                when (service.targetType) {
                    "NATIVE_ACTIVITY" -> launchNativeActivity(service.targetClass, service.title)
                    else -> {
                        if (service.url.isNotEmpty()) {
                            openServiceWebView(service)
                        } else {
                            android.widget.Toast.makeText(this, "${service.title} service is coming soon", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            binding.gridServices.addView(bindingItem.root)
        }

        binding.layoutCategoriesScreen.visibility = View.GONE
        binding.layoutServicesListScreen.visibility = View.VISIBLE

        binding.btnBackContainer.visibility = View.VISIBLE
    }

    private fun launchNativeActivity(className: String, title: String) {
        try {
            if (className.isNotEmpty()) {
                val clazz = Class.forName(className)
                val intent = Intent(this, clazz)
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(this, "$title screen coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Could not launch screen: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
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
