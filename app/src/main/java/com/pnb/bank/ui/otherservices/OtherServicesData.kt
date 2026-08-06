package com.pnb.bank.ui.otherservices

import android.content.Context
import org.json.JSONArray

data class ServiceItem(
    val title: String,
    val iconEmoji: String,
    val url: String
)

data class ServiceCategory(
    val id: String,
    val title: String,
    val iconEmoji: String,
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
                val id = catObj.optString("id", "")
                val title = catObj.optString("title", "")
                val iconEmoji = catObj.optString("iconEmoji", "📄")

                val servicesArray = catObj.getJSONArray("services")
                val servicesList = mutableListOf<ServiceItem>()

                for (j in 0 until servicesArray.length()) {
                    val servObj = servicesArray.getJSONObject(j)
                    val sTitle = servObj.optString("title", "")
                    val sIcon = servObj.optString("iconEmoji", "🔗")
                    val sUrl = servObj.optString("url", "")

                    servicesList.add(ServiceItem(sTitle, sIcon, sUrl))
                }

                categoriesList.add(ServiceCategory(id, title, iconEmoji, servicesList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categoriesList
    }
}
