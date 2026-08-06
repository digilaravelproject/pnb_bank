package com.pnb.bank.data.api.models

import com.google.gson.annotations.SerializedName

// 1. Validate Customer API Models
data class ValidateCustomerRequest(
    @SerializedName("panNumber") val panNumber: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("requestId") val requestId: String
)

data class ValidateCustomerResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("refId") val refId: String?,
    @SerializedName("responseId") val responseId: String?,
    @SerializedName("otpSent") val otpSent: Boolean?
)

// 2. Verify OTP API Models
data class VerifyOtpRequest(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("eligibleCardVariants") val eligibleCardVariants: List<String>?
)

// 3. Link Card API Models
data class LinkCardRequest(
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("cardNumber") val cardNumber: String,
    @SerializedName("cardVariant") val cardVariant: String,
    @SerializedName("customerName") val customerName: String
)

data class LinkCardResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("cardStatus") val cardStatus: String?
)

// Generic API Error Response Model
data class ApiErrorResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("message") val message: String?
)
