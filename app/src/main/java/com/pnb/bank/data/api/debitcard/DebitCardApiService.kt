package com.pnb.bank.data.api.debitcard

import com.pnb.bank.data.api.ApiConstants
import com.pnb.bank.data.api.debitcard.models.GenerateOtpRequest
import com.pnb.bank.data.api.debitcard.models.GenerateOtpResponse
import com.pnb.bank.data.api.debitcard.models.LinkCardRequest
import com.pnb.bank.data.api.debitcard.models.LinkCardResponse
import com.pnb.bank.data.api.debitcard.models.ValidateCustomerRequest
import com.pnb.bank.data.api.debitcard.models.ValidateCustomerResponse
import com.pnb.bank.data.api.debitcard.models.VerifyOtpRequest
import com.pnb.bank.data.api.debitcard.models.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DebitCardApiService {

    /**
     * Validate Customer & Fetch Customer Details
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/validateCustomer
     */
    @POST(ApiConstants.ENDPOINT_VALIDATE_CUSTOMER)
    suspend fun validateCustomer(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: ValidateCustomerRequest
    ): Response<ValidateCustomerResponse>

    /**
     * Generate OTP to Customer Mobile Number
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/generateOtp
     */
    @POST(ApiConstants.ENDPOINT_GENERATE_OTP)
    suspend fun generateOtp(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: GenerateOtpRequest
    ): Response<GenerateOtpResponse>

    /**
     * Verify OTP
     * POST: https://apisit.vakrangee.in/pg/api/v1/debitcard/verify-otp
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

    /**
     * Fetch Encrypted Credit Score
     */
    @POST(ApiConstants.ENDPOINT_GET_CREDIT_SCORE)
    suspend fun getCreditScore(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Header("Client-Id") clientId: String = "@n|)r0||)@tm",
        @Body request: com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsRequest
    ): Response<com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsResponse>

    /**
     * Fetch Credit Score Plain
     */
    @POST(ApiConstants.ENDPOINT_GET_PLAIN_CREDIT_SCORE)
    suspend fun getCreditScorePlain(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Header("Client-Id") clientId: String = "@n|)r0||)@tm",
        @Body request: com.pnb.bank.data.api.bankgateway.models.CreditScoreRequest
    ): Response<com.pnb.bank.data.api.bankgateway.models.CreditScoreResponse>
}
