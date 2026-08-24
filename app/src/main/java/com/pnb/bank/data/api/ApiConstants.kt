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
    var bankAccessToken: String = ""
    var tokenFetchTimestamp: Long = 0L
    var tokenExpiresInSeconds: Long = 86398L

    var activeCardNumber: String = AppConstants.DEFAULT_CARD_NUMBER




    fun isBankTokenValid(): Boolean {
        if (bankAccessToken.trim().isEmpty()) return false
        val currentTime = System.currentTimeMillis()
        val tokenAgeMs = currentTime - tokenFetchTimestamp
        val expiryMs = (tokenExpiresInSeconds - 60) * 1000L // 60s safety buffer before expiry
        return tokenAgeMs < expiryMs
    }

    fun getFormattedBearerToken(): String {
        val token = activeBearerToken.trim()
        if (token.isEmpty()) return AppConstants.DEFAULT_BEARER_TOKEN
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }

    fun getFormattedBankBearerToken(): String {
        val token = bankAccessToken.trim()
        if (token.isEmpty()) return getFormattedBearerToken()
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }


    // API Endpoints
    const val ENDPOINT_VALIDATE_CUSTOMER = "pg/api/v1/debitcard/validateCustomer"
    const val ENDPOINT_GENERATE_OTP = "pg/api/v1/debitcard/generateOtp"
    const val ENDPOINT_VERIFY_OTP = "pg/api/v1/debitcard/verify-otp"
    const val ENDPOINT_LINK_CARD = "pg/api/v1/debitcard/linkCard"

    // Bank OAuth & Customer Details Endpoints & Credentials
    const val ENDPOINT_OAUTH_TOKEN = "pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/AccessTokenService"
    const val ENDPOINT_CUSTOMER_DETAILS = "pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetails"
    const val ENDPOINT_CUSTOMER_DETAILS_PLAIN = "pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetailsPlain"

    // Bank OAuth Credentials & Settings
    const val BANK_GRANT_TYPE = "password"
    const val BANK_USERNAME = "AndroidATMUser"
    const val BANK_PASSWORD = "K7#mP2@x"
    const val BANK_CHANNEL_ID = "ATM_ANDROID"
    const val BANK_REQUEST_ID = "123"

    // Encryption Toggle Flag & AES Key
    var IS_ENCRYPTION_ENABLED: Boolean = true // Set to false for Plain API (CustomerDetailsPlain), true for Encrypted API (CustomerDetails)
    const val BANK_ENCRYPTION_KEY = "bf91a235b5b64858bdb2d87d0f238d8d"
}







