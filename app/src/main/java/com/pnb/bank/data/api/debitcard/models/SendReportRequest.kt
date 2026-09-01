package com.pnb.bank.data.api.debitcard.models

import com.google.gson.annotations.SerializedName

data class SendReportRequest(
    @SerializedName("name") val name: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("pan") val pan: String,
    @SerializedName("email") val email: String
)

data class SendReportResponse(
    @SerializedName("statuscode") val statuscode: Int?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("reference_id") val referenceId: String?,
    @SerializedName("data") val data: Any?
)
