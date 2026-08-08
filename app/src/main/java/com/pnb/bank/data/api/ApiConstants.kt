package com.pnb.bank.data.api

import com.pnb.bank.utils.AppConstants

object ApiConstants {

    // Base URL Configuration
    const val BASE_URL = "https://apisit.vakrangee.in/"

    // Network Timeouts (in Seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // HTTP Header Keys
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_AUTHORIZATION = "Authorization"
    const val VALUE_APPLICATION_JSON = "application/json"

    // Dynamic Session Variables
    var activeBearerToken: String = AppConstants.DEFAULT_BEARER_TOKEN
    val activeRequestId: String
        get() = System.currentTimeMillis().toString()
    var activeCardNumber: String = AppConstants.DEFAULT_CARD_NUMBER

    fun getFormattedBearerToken(): String {
        val token = activeBearerToken.trim()
        if (token.isEmpty()) return AppConstants.DEFAULT_BEARER_TOKEN
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }

    // API Endpoints
    const val ENDPOINT_VALIDATE_CUSTOMER = "pg/api/v1/debitcard/validateCustomer"
    const val ENDPOINT_VERIFY_OTP = "pg/api/v1/debitcard/verifyOtp"
    const val ENDPOINT_LINK_CARD = "pg/api/v1/debitcard/linkCard"
}
