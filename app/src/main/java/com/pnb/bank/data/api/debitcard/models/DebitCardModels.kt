package com.pnb.bank.data.api.debitcard.models

import com.google.gson.annotations.SerializedName

// 1. Validate Customer API Models
data class ValidateCustomerRequest(
    @SerializedName("panNumber") val panNumber: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("requestId") val requestId: String
)

data class ValidateCustomerResponse(
    @SerializedName("transactionId") val transactionId: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("middleName") val middleName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("customerMobileNumber") val customerMobileNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("refId") val refId: String?,
    @SerializedName("responseId") val responseId: String?,
    @SerializedName("otpSent") val otpSent: Boolean?
)

// 2. Generate OTP API Models
data class GenerateOtpRequest(
    @SerializedName("mobileNumber") val mobileNumber: String
)

data class GenerateOtpResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("otpSent") val otpSent: Boolean?
)

// 3. Verify OTP API Models
data class VerifyOtpRequest(
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("eligibleCardVariants") val eligibleCardVariants: List<String>? = null
)

// 4. Un-map Card API Models
data class UnmapCardRequest(
    @SerializedName("panNumber") val panNumber: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("requestId") val requestId: String,
    @SerializedName("customerName") val customerName: String
)

data class UnmapCardResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("cardStatus") val cardStatus: String?
)

// 5. Link Card API Models
data class LinkCardRequest(
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("cardNumber") val cardNumber: String,
    @SerializedName("cardVariant") val cardVariant: String,
    @SerializedName("customerName") val customerName: String
)

data class LinkCardResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("cardStatus") val cardStatus: String?,
    @SerializedName("responseMessage") val responseMessage: String? = null
)

// Generic API Error Response Model
data class ApiErrorResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("responseMessage") val responseMessage: String?,
    @SerializedName("message") val message: String?
)
