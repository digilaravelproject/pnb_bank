package com.pnb.bank.data.api

import com.pnb.bank.data.api.models.LinkCardRequest
import com.pnb.bank.data.api.models.LinkCardResponse
import com.pnb.bank.data.api.models.ValidateCustomerRequest
import com.pnb.bank.data.api.models.ValidateCustomerResponse
import com.pnb.bank.data.api.models.VerifyOtpRequest
import com.pnb.bank.data.api.models.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    /**
     * Validate Customer & Generate OTP
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/validateCustomer
     */
    @POST(ApiConstants.ENDPOINT_VALIDATE_CUSTOMER)
    suspend fun validateCustomer(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: ValidateCustomerRequest
    ): Response<ValidateCustomerResponse>

    /**
     * Verify OTP & Fetch Customer Name and Eligible Card Variants
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/verifyOtp
     */
    @POST(ApiConstants.ENDPOINT_VERIFY_OTP)
    suspend fun verifyOtp(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    /**
     * Link Card & Issue Debit Card
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/linkCard
     */
    @POST(ApiConstants.ENDPOINT_LINK_CARD)
    suspend fun linkCard(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: LinkCardRequest
    ): Response<LinkCardResponse>
}
