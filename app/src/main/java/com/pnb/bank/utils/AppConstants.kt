package com.pnb.bank.utils

object AppConstants {
    // Intent Keys (passed directly from Parent App)
    const val KEY_SERVICE = "service_key"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_CARD_NUMBER = "card_number"
    const val KEY_RESULT_DATA = "result_data"

    // Parent App Package Name Constant
    const val PARENT_APP_PACKAGE_NAME = "com.pnb.parentapp"

    // Service Keys
    const val SERVICE_CARD_REISSUANCE = "card_reissuance"
    const val SERVICE_OTHER_SERVICES = "other_services"

    // =========================================================================
    // TESTING FALLBACK CONSTANTS (Toggle all 3 together)
    // =========================================================================
    // --- MODE 1: Standalone Testing Mode (ACTIVE) ---
    const val DEFAULT_BEARER_TOKEN = "Bearer 8fd79dbb-04fd-48bc-99db-c1b738bf72bx"
    const val DEFAULT_CARD_NUMBER = "6522123499998888"
    const val DEFAULT_CARD_VARIANT = "RUPAY_SELECT"
    const val TEST_SERVICE_KEY = SERVICE_OTHER_SERVICES

    // --- MODE 2: Parent App Intent Mode (Commented Out for Testing) ---
    // const val DEFAULT_BEARER_TOKEN = ""
    // const val DEFAULT_CARD_NUMBER = ""
    // const val TEST_SERVICE_KEY = ""
    // =========================================================================

    // Test Auto-Fill Form Helpers
    data class TestAccountData(
        val accountNumber: String,
        val panNumber: String
    )

    val TEST_ACCOUNTS_LIST = listOf(
        TestAccountData("666444222", "ABCDE1234F"),
        TestAccountData("105205305", "ABCDE1234F"),
        TestAccountData("201301401", "ABCDE1234F"),
        TestAccountData("102202302", "ABCDE1234F")
    )
}
