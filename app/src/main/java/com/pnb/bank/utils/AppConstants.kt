package com.pnb.bank.utils

object AppConstants {
    // Intent Keys (passed from parent app)
    const val KEY_SERVICE = "service_key"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_REQUEST_ID = "request_id"
    const val KEY_CARD_NUMBER = "card_number"

    // Service Keys
    const val SERVICE_CARD_REISSUANCE = "card_reissuance"
    const val SERVICE_OTHER_SERVICES = "other_services"

    // Default Testing Fallback Data
    const val DEFAULT_BEARER_TOKEN = "Bearer 8fd79dbb-04fd-48bc-99db-c1b738bf72bx"
    const val DEFAULT_REQUEST_ID = "121212"
    const val DEFAULT_CARD_NUMBER = "6522123412341234"

    // Test Auto-Fill Data
    const val TEST_ACCOUNT_NUMBER = "666444222"
    const val TEST_PAN_NUMBER = "ABCDE1234F"

    // Testing Fallback Service Key
    const val TEST_SERVICE_KEY = SERVICE_CARD_REISSUANCE
}
