package com.pnb.bank.data.api.bankgateway.models

import com.google.gson.annotations.SerializedName

// 1. Bank OAuth Token Models
data class BankOAuthTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("scope") val scope: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long?
)

// 2. Encrypted Customer Details Models
data class EncryptedCustomerDetailsRequest(
    @SerializedName("EncReqData") val encReqData: String
)

data class EncryptedCustomerDetailsResponse(
    @SerializedName("EncRespData") val encRespData: String?
)

// 3. Customer Details Plain Models
data class CustomerDetailsPlainRequest(
    @SerializedName("IN_MOBILE") val inMobile: String,
    @SerializedName("IN_REQID") val inReqId: String = com.pnb.bank.data.api.ApiConstants.BANK_REQUEST_ID,
    @SerializedName("IN_FORACID") val inForAcid: String,
    @SerializedName("CHANNEL_ID") val channelId: String = com.pnb.bank.data.api.ApiConstants.BANK_CHANNEL_ID
)




data class CustomerDetailsPlainResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("remarks") val remarks: String?,
    @SerializedName("resultData") val resultData: CustomerResultData?
)

data class CustomerResultData(
    @SerializedName("mobileNo") val mobileNo: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("middleName") val middleName: String? = null,
    @SerializedName("lastName") val lastName: String?
)
