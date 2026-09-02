package com.pnb.bank.data.api.bankgateway.models

import com.google.gson.annotations.SerializedName

// Encrypted Request
data class EncryptedCkycRequest(
    @SerializedName("EncReqData")
    val encReqData: String
)

// Plain Request
data class PlainCkycRequest(
    @SerializedName("accNum")
    val accNum: String
)

// Encrypted Response
data class EncryptedCkycResponse(
    @SerializedName("EncRespData")
    val encRespData: String?
)

// Plain Response
data class PlainCkycResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("ckycNum")
    val ckycNum: String?,
    @SerializedName("remarks")
    val remarks: String?
)
