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

// 4. Credit Score Request / Response Models
data class CreditScoreRequest(
    @SerializedName("refid") val refId: String,
    @SerializedName("name") val name: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("document_id") val documentId: String
)

data class CreditScoreResponse(
    @SerializedName("statuscode") val statusCode: Int?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("reference_id") val referenceId: Long?,
    @SerializedName("data") val data: CreditScoreData?
)

data class CreditScoreData(
    @SerializedName("ccrresponse") val ccrResponse: CcrResponse?
)

data class CcrResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("cirreportDataLst") val cirReportDataList: List<CirReportItem>?
)

data class CirReportItem(
    @SerializedName("cirreportData") val cirReportData: CirReportData?
)

data class CirReportData(
    @SerializedName("scoreDetails") val scoreDetails: List<ScoreDetail>?,
    @SerializedName("idandContactInfo") val idAndContactInfo: IdAndContactInfo?
)

data class ScoreDetail(
    @SerializedName("type") val type: String?,
    @SerializedName("version") val version: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?
)

data class IdAndContactInfo(
    @SerializedName("personalInfo") val personalInfo: PersonalInfo?
)

data class PersonalInfo(
    @SerializedName("name") val name: PersonalName?,
    @SerializedName("dateOfBirth") val dateOfBirth: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("age") val age: PersonalAge?
)

data class PersonalName(
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
)

data class PersonalAge(
    @SerializedName("age") val age: String?
)
