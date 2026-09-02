package com.pnb.bank.data.api.bankgateway

import com.pnb.bank.data.api.ApiConstants
import com.pnb.bank.data.api.bankgateway.models.BankOAuthTokenResponse
import com.pnb.bank.data.api.bankgateway.models.CustomerDetailsPlainRequest
import com.pnb.bank.data.api.bankgateway.models.CustomerDetailsPlainResponse
import com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsRequest
import com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface BankApiService {

    /**
     * 1. Fetch Bank OAuth Access Token
     * POST: https://apisit.vakrangee.in/pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/AccessTokenService
     */
    @FormUrlEncoded
    @POST(ApiConstants.ENDPOINT_OAUTH_TOKEN)
    suspend fun getBankAccessToken(
        @Field("grant_type") grantType: String = ApiConstants.BANK_GRANT_TYPE,
        @Field("username") username: String = ApiConstants.BANK_USERNAME,
        @Field("password") password: String = ApiConstants.BANK_PASSWORD
    ): Response<BankOAuthTokenResponse>


    /**
     * 2. Fetch Encrypted Customer Details
     * POST: https://apisit.vakrangee.in/pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetails
     */
    @POST(ApiConstants.ENDPOINT_CUSTOMER_DETAILS)
    suspend fun getCustomerDetails(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: EncryptedCustomerDetailsRequest
    ): Response<EncryptedCustomerDetailsResponse>

    /**
     * 3. Fetch Customer Details Plain
     * POST: https://apisit.vakrangee.in/pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetailsPlain
     */
    @POST(ApiConstants.ENDPOINT_CUSTOMER_DETAILS_PLAIN)
    suspend fun getCustomerDetailsPlain(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: CustomerDetailsPlainRequest
    ): Response<CustomerDetailsPlainResponse>

    /**
     * 4. Fetch CKYC Number
     * POST: https://apisit.vakrangee.in/pg/privategateway/1/AndoridATMChannel/ATMSG/v1/CKYCNumFromAccNum
     */
    @POST(ApiConstants.ENDPOINT_CKYC)
    suspend fun getCkycNumber(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: com.pnb.bank.data.api.bankgateway.models.EncryptedCkycRequest
    ): Response<com.pnb.bank.data.api.bankgateway.models.EncryptedCkycResponse>

    /**
     * 5. Fetch CKYC Number (Plain)
     * POST: https://apisit.vakrangee.in/pg/privategateway/1/AndoridATMChannel/ATMSG/v1/CKYCNumFromAccNum
     */
    @POST(ApiConstants.ENDPOINT_CKYC)
    suspend fun getCkycNumberPlain(
        @Header(ApiConstants.HEADER_AUTHORIZATION) token: String? = null,
        @Body request: com.pnb.bank.data.api.bankgateway.models.PlainCkycRequest
    ): Response<com.pnb.bank.data.api.bankgateway.models.PlainCkycResponse>
}
