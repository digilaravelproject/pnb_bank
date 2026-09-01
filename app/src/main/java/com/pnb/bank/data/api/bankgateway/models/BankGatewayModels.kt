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
    @SerializedName("refid") val refId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("pan") val pan: String? = null,
    @SerializedName("document_id") val documentId: String? = null
)

data class CreditScoreResponse(
    @SerializedName("statuscode") val statusCode: Int?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("reference_id") val referenceId: Long?,
    @SerializedName("data") val data: CreditScoreData?
)

data class CreditScoreData(
    @SerializedName("ccrresponse") val ccrResponse: CcrResponse? = null,
    @SerializedName("credit_score") val creditScore: Int? = null,
    @SerializedName("pan") val pan: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("credit_report") val creditReport: CreditReportV3? = null
)

data class CreditReportV3(
    @SerializedName("CreditProfileHeader") val creditProfileHeader: CreditProfileHeaderV3? = null,
    @SerializedName("Current_Application") val currentApplication: CurrentApplicationV3? = null,
    @SerializedName("SCORE") val score: ScoreV3? = null
)

data class CreditProfileHeaderV3(
    @SerializedName("ReportDate") val reportDate: Long? = null,
    @SerializedName("ReportTime") val reportTime: Long? = null,
    @SerializedName("Version") val version: String? = null,
    @SerializedName("ReportNumber") val reportNumber: String? = null
)

data class CurrentApplicationV3(
    @SerializedName("Current_Application_Details") val currentApplicationDetails: CurrentApplicationDetailsV3? = null
)

data class CurrentApplicationDetailsV3(
    @SerializedName("Current_Applicant_Details") val currentApplicantDetails: CurrentApplicantDetailsV3? = null
)

data class CurrentApplicantDetailsV3(
    @SerializedName("First_Name") val firstName: String? = null,
    @SerializedName("Last_Name") val lastName: String? = null,
    @SerializedName("Middle_Name1") val middleName1: String? = null,
    @SerializedName("Gender_Code") val genderCode: String? = null,
    @SerializedName("IncomeTaxPan") val incomeTaxPan: String? = null,
    @SerializedName("Date_Of_Birth_Applicant") val dateOfBirth: String? = null,
    @SerializedName("MobilePhoneNumber") val mobilePhoneNumber: String? = null,
    @SerializedName("EMailId") val emailId: String? = null
)

data class ScoreV3(
    @SerializedName("FCIREXScore") val fcirexScore: Int? = null,
    @SerializedName("FCIREXScoreConfidLevel") val fcirexScoreConfidLevel: String? = null
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
