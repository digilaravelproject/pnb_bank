package com.pnb.bank.utils

object AppConstants {
    // Intent Keys
    const val KEY_SERVICE = "service_key"

    // Service Keys (sent by parent app)
    const val SERVICE_CARD_REISSUANCE = "card_reissuance"
    const val SERVICE_OTHER_SERVICES = "other_services"

    // Testing fallback
    const val TEST_SERVICE_KEY = SERVICE_CARD_REISSUANCE
}
