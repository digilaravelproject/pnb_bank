package com.pnb.bank.ui.otherservices

import android.content.Context
import org.json.JSONArray

data class ServiceItem(
    val title: String,
    val iconEmoji: String,
    val url: String,
    val targetType: String = "WEBVIEW", // WEBVIEW, NATIVE_ACTIVITY, SUB_LIST
    val targetClass: String = ""
)

data class ServiceCategory(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val url: String = "",
    val targetType: String = "SUB_LIST", // SUB_LIST, WEBVIEW, NATIVE_ACTIVITY
    val targetClass: String = "",
    val services: List<ServiceItem>
)

object OtherServicesRepository {

    fun loadCategoriesFromJson(context: Context): List<ServiceCategory> {
        val categoriesList = mutableListOf<ServiceCategory>()
        try {
            val jsonString = context.assets.open("services.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val catObj = jsonArray.getJSONObject(i)
                val enabled = catObj.optBoolean("enabled", true)
                if (!enabled) continue

                val id = catObj.optString("id", "")
                val title = catObj.optString("title", "")
                val iconEmoji = catObj.optString("iconEmoji", "📄")
                val url = catObj.optString("url", "")
                val targetType = catObj.optString("targetType", if (catObj.has("services") && catObj.getJSONArray("services").length() > 0) "SUB_LIST" else "WEBVIEW")
                val targetClass = catObj.optString("targetClass", "")

                val servicesArray = catObj.optJSONArray("services")
                val servicesList = mutableListOf<ServiceItem>()

                if (servicesArray != null) {
                    for (j in 0 until servicesArray.length()) {
                        val servObj = servicesArray.getJSONObject(j)
                        val sEnabled = servObj.optBoolean("enabled", true)
                        if (!sEnabled) continue

                        val sTitle = servObj.optString("title", "")
                        val sIcon = servObj.optString("iconEmoji", "🔗")
                        val sUrl = servObj.optString("url", "")
                        val sTargetType = servObj.optString("targetType", "WEBVIEW")
                        val sTargetClass = servObj.optString("targetClass", "")

                        servicesList.add(ServiceItem(sTitle, sIcon, sUrl, sTargetType, sTargetClass))
                    }
                }

                categoriesList.add(ServiceCategory(id, title, iconEmoji, url, targetType, targetClass, servicesList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categoriesList
    }
}
