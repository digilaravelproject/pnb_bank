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
    const val DEFAULT_CARD_VARIANT = "DEFAULT_CARD_NUMBER"
    const val TEST_SERVICE_KEY = SERVICE_OTHER_SERVICES

    // --- MODE 2: Parent App Intent Mode (Commented Out for Testing) ---
    // const val DEFAULT_BEARER_TOKEN = ""
    // const val DEFAULT_CARD_NUMBER = ""
    // const val TEST_SERVICE_KEY = ""
    // =========================================================================

    // =========================================================================
    // FEATURE TOGGLES & TEST CONSTANTS
    // =========================================================================
    var IS_PAN_MODE_ENABLED: Boolean = false       // true = PAN Mode, false = Mobile Mode
    var IS_DYNAMIC_MOBILE_ENABLED: Boolean = false // true = Dynamic Fetched Mobile, false = Test Mobile Number
    const val DEFAULT_OTP_MOBILE_NUMBER = "7458086472"
    // =========================================================================





    // 1. PAN Mode Test Accounts
    data class TestPanAccountData(
        val accountNumber: String,
        val panNumber: String
    )

    val TEST_PAN_ACCOUNTS_LIST = listOf(
        TestPanAccountData("666444222", "ABCDE1234F"),
        TestPanAccountData("105205305", "ABCDE1234F"),
        TestPanAccountData("201301401", "ABCDE1234F"),
        TestPanAccountData("102202302", "ABCDE1234F")
    )

    // 2. Mobile Mode Test Accounts
    data class TestMobileAccountData(
        val accountNumber: String,
        val mobileNumber: String
    )

    val TEST_MOBILE_ACCOUNTS_LIST = listOf(
        TestMobileAccountData("808080123", "919843746860"),
        TestMobileAccountData("666444222", "7757011027")
    )

    // 3. Credit Score Test Constants
    data class TestCreditScoreData(
        val name: String,
        val mobile: String,
        val documentId: String,
        val email: String
    )

    val TEST_CREDIT_SCORE_LIST = listOf(
        TestCreditScoreData("Chetan Manohar Gholekar", "9892650971", "AMJPG2624D", "cmgthebest@gmail.com"),
        TestCreditScoreData("Deepak Kumar", "9892945964", "BBHPT3778F", "deepak@example.com"),
        TestCreditScoreData("Manish Gupta", "8889998889", "QWERT1234Y", "manish@example.com")
    )
}









