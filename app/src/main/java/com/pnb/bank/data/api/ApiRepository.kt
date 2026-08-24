package com.pnb.bank.data.api

import com.google.gson.Gson
import com.pnb.bank.data.api.bankgateway.BankApiClient
import com.pnb.bank.data.api.bankgateway.BankApiService
import com.pnb.bank.data.api.bankgateway.models.BankOAuthTokenResponse
import com.pnb.bank.data.api.bankgateway.models.CustomerDetailsPlainRequest
import com.pnb.bank.data.api.bankgateway.models.CustomerDetailsPlainResponse
import com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsRequest
import com.pnb.bank.data.api.bankgateway.models.EncryptedCustomerDetailsResponse
import com.pnb.bank.data.api.debitcard.DebitCardApiClient
import com.pnb.bank.data.api.debitcard.DebitCardApiService
import com.pnb.bank.data.api.debitcard.models.ApiErrorResponse
import com.pnb.bank.data.api.debitcard.models.GenerateOtpRequest
import com.pnb.bank.data.api.debitcard.models.GenerateOtpResponse
import com.pnb.bank.data.api.debitcard.models.LinkCardRequest
import com.pnb.bank.data.api.debitcard.models.LinkCardResponse
import com.pnb.bank.data.api.debitcard.models.ValidateCustomerRequest
import com.pnb.bank.data.api.debitcard.models.ValidateCustomerResponse
import com.pnb.bank.data.api.debitcard.models.VerifyOtpRequest
import com.pnb.bank.data.api.debitcard.models.VerifyOtpResponse
import com.pnb.bank.utils.AppLogger
import com.pnb.bank.utils.BankCryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ApiRepository(
    private val apiService: DebitCardApiService = DebitCardApiClient.apiService,
    private val bankApiService: BankApiService = BankApiClient.apiService
) {


    private val gson = Gson()

    /**
     * Unified Customer Details Fetch Method
     * Dynamically chooses between CustomerDetails (Encrypted AES/GCM) and CustomerDetailsPlain based on ApiConstants.IS_ENCRYPTION_ENABLED
     */
    suspend fun fetchCustomerDetailsUnified(
        accountNumber: String,
        mobileNumber: String,
        requestId: String = ApiConstants.BANK_REQUEST_ID
    ): NetworkResult<CustomerDetailsPlainResponse> {

        return if (ApiConstants.IS_ENCRYPTION_ENABLED) {
            AppLogger.i("Executing Encrypted CustomerDetails API (IS_ENCRYPTION_ENABLED = true)")
            val plainRequest = CustomerDetailsPlainRequest(
                inMobile = mobileNumber,
                inReqId = requestId,
                inForAcid = accountNumber,
                channelId = ApiConstants.BANK_CHANNEL_ID
            )
            val plainJson = gson.toJson(plainRequest)
            AppLogger.d("Plain Request JSON to Encrypt: $plainJson")

            val encReqData = try {
                BankCryptoUtils.encrypt(plainJson)
            } catch (e: Exception) {
                return NetworkResult.Error(code = null, message = "Encryption Error: ${e.localizedMessage}", exception = e)
            }

            val encResult = getCustomerDetails(encReqData = encReqData)
            when (encResult) {
                is NetworkResult.Success -> {
                    val encRespData = encResult.data.encRespData
                    AppLogger.i("Encrypted Response EncRespData Received: $encRespData")
                    if (!encRespData.isNullOrEmpty()) {
                        try {
                            val decryptedJson = BankCryptoUtils.decrypt(encRespData)
                            AppLogger.i("Decrypted Response JSON Successfully: $decryptedJson")
                            val parsedResponse = gson.fromJson(decryptedJson, CustomerDetailsPlainResponse::class.java)
                            NetworkResult.Success(parsedResponse)
                        } catch (e: Exception) {
                            AppLogger.e("Decryption Error for CustomerDetails: ${e.message}", e)
                            NetworkResult.Error(code = null, message = "Decryption Failed: ${e.localizedMessage}", exception = e)
                        }
                    } else {
                        AppLogger.w("EncRespData is null or empty in server response")
                        NetworkResult.Error(code = null, message = "EncRespData is null or empty")
                    }
                }

                is NetworkResult.Error -> {
                    NetworkResult.Error(code = encResult.code, message = encResult.message, exception = encResult.exception)
                }
                is NetworkResult.Loading -> NetworkResult.Loading
            }
        } else {
            AppLogger.i("Executing Plain CustomerDetailsPlain API (IS_ENCRYPTION_ENABLED = false)")
            getCustomerDetailsPlain(accountNumber = accountNumber, mobileNumber = mobileNumber, requestId = requestId)
        }
    }

    /**
     * Unified Credit Score Fetch Method
     * Dynamically chooses between GetCreditScore (Encrypted AES/GCM) and GetPlainCreditScore based on ApiConstants.IS_ENCRYPTION_ENABLED
     */
    suspend fun fetchCreditScoreUnified(
        name: String,
        mobileNumber: String,
        panNumber: String
    ): NetworkResult<com.pnb.bank.data.api.bankgateway.models.CreditScoreResponse> {
        val refId = System.currentTimeMillis().toString()
        val plainRequest = com.pnb.bank.data.api.bankgateway.models.CreditScoreRequest(
            refId = refId,
            name = name,
            mobile = mobileNumber,
            documentId = panNumber
        )

        return if (ApiConstants.IS_ENCRYPTION_ENABLED) {
            AppLogger.i("Executing Encrypted CreditScore API (IS_ENCRYPTION_ENABLED = true)")
            val plainJson = gson.toJson(plainRequest)
            AppLogger.d("Plain Request JSON to Encrypt: $plainJson")

            val encReqData = try {
                BankCryptoUtils.encrypt(plainJson)
            } catch (e: Exception) {
                return NetworkResult.Error(code = null, message = "Encryption Error: ${e.localizedMessage}", exception = e)
            }

            val finalResult = safeApiCall {
                apiService.getCreditScore(
                    token = ApiConstants.getFormattedBearerToken(),
                    request = EncryptedCustomerDetailsRequest(encReqData = encReqData)
                )
            }

            when (finalResult) {
                is NetworkResult.Success -> {
                    val encRespData = finalResult.data.encRespData
                    AppLogger.i("Encrypted Response EncRespData Received: $encRespData")
                    if (!encRespData.isNullOrEmpty()) {
                        try {
                            val decryptedJson = BankCryptoUtils.decrypt(encRespData)
                            AppLogger.i("Decrypted Response JSON Successfully: $decryptedJson")
                            val parsedResponse = gson.fromJson(decryptedJson, com.pnb.bank.data.api.bankgateway.models.CreditScoreResponse::class.java)
                            NetworkResult.Success(parsedResponse)
                        } catch (e: Exception) {
                            AppLogger.e("Decryption Error for CreditScore: ${e.message}", e)
                            NetworkResult.Error(code = null, message = "Decryption Failed: ${e.localizedMessage}", exception = e)
                        }
                    } else {
                        AppLogger.w("EncRespData is null or empty in server response")
                        NetworkResult.Error(code = null, message = "EncRespData is null or empty")
                    }
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(code = finalResult.code, message = finalResult.message, exception = finalResult.exception)
                }
                is NetworkResult.Loading -> NetworkResult.Loading
            }
        } else {
            AppLogger.i("Executing Plain GetPlainCreditScore API (IS_ENCRYPTION_ENABLED = false)")
            val finalResult = safeApiCall {
                apiService.getCreditScorePlain(
                    token = ApiConstants.getFormattedBearerToken(),
                    request = plainRequest
                )
            }
            finalResult
        }
    }

    /**
     * Fetch Bank OAuth Access Token Service with Auto Expiry Check & Refresh
     */
    suspend fun getBankAccessToken(forceRefresh: Boolean = false): NetworkResult<BankOAuthTokenResponse> {
        if (!forceRefresh && ApiConstants.isBankTokenValid()) {
            AppLogger.i("Using valid cached Bank OAuth Access Token")
            return NetworkResult.Success(
                BankOAuthTokenResponse(
                    accessToken = ApiConstants.bankAccessToken,
                    scope = "ATMScope",
                    tokenType = "Bearer",
                    expiresIn = ApiConstants.tokenExpiresInSeconds
                )
            )
        }

        AppLogger.i("Fetching new Bank OAuth Access Token (Token expired or force refresh)...")
        val result = safeApiCall {
            bankApiService.getBankAccessToken()
        }

        if (result is NetworkResult.Success) {
            val token = result.data.accessToken
            if (!token.isNullOrEmpty()) {
                ApiConstants.bankAccessToken = token
                ApiConstants.tokenFetchTimestamp = System.currentTimeMillis()
                ApiConstants.tokenExpiresInSeconds = result.data.expiresIn ?: 86398L
                AppLogger.i("New Bank OAuth Access Token cached successfully (expires_in: ${ApiConstants.tokenExpiresInSeconds}s): ${token.take(15)}...")
            }
        }
        return result
    }

    /**
     * Fetch Encrypted Customer Details API Call
     * Endpoint: POST pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetails
     */
    suspend fun getCustomerDetails(
        encReqData: String
    ): NetworkResult<EncryptedCustomerDetailsResponse> {
        if (!ApiConstants.isBankTokenValid()) {
            val tokenResult = getBankAccessToken(forceRefresh = true)
            if (tokenResult is NetworkResult.Error) {
                return NetworkResult.Error(code = tokenResult.code, message = "Failed to fetch OAuth token: ${tokenResult.message}")
            }
        }

        val request = EncryptedCustomerDetailsRequest(encReqData = encReqData)
        AppLogger.i("Executing getCustomerDetails Encrypted API")

        val result = safeApiCall {
            bankApiService.getCustomerDetails(
                token = ApiConstants.getFormattedBankBearerToken(),
                request = request
            )
        }

        // Auto-retry once if 401 Unauthorized occurs
        if (result is NetworkResult.Error && result.code == 401) {
            AppLogger.w("401 Unauthorized received for getCustomerDetails, force refreshing OAuth token & retrying...")
            val tokenRefresh = getBankAccessToken(forceRefresh = true)
            if (tokenRefresh is NetworkResult.Success) {
                return safeApiCall {
                    bankApiService.getCustomerDetails(
                        token = ApiConstants.getFormattedBankBearerToken(),
                        request = request
                    )
                }
            }
        }

        return result
    }

    /**
     * Fetch Customer Details Plain API Call
     * Endpoint: POST pg/privategateway/1/OAuthPrivateChannel/OAuth2PrivateSG/v1/CustomerDetailsPlain
     */
    suspend fun getCustomerDetailsPlain(
        accountNumber: String,
        mobileNumber: String,
        requestId: String = ApiConstants.BANK_REQUEST_ID
    ): NetworkResult<CustomerDetailsPlainResponse> {
        // Ensure valid unexpired token exists
        if (!ApiConstants.isBankTokenValid()) {
            AppLogger.w("Bank OAuth Token expired or missing. Fetching new token before calling CustomerDetailsPlain...")
            val tokenResult = getBankAccessToken(forceRefresh = true)
            if (tokenResult is NetworkResult.Error) {
                return NetworkResult.Error(code = tokenResult.code, message = "Failed to fetch OAuth token: ${tokenResult.message}")
            }
        }

        val request = CustomerDetailsPlainRequest(
            inMobile = mobileNumber,
            inReqId = requestId,
            inForAcid = accountNumber,
            channelId = ApiConstants.BANK_CHANNEL_ID
        )

        AppLogger.i("Executing getCustomerDetailsPlain API for Account: $accountNumber | Mobile: $mobileNumber")

        val result = safeApiCall {
            bankApiService.getCustomerDetailsPlain(
                token = ApiConstants.getFormattedBankBearerToken(),
                request = request
            )
        }

        // Auto-retry once with fresh token if 401 Unauthorized occurs
        if (result is NetworkResult.Error && result.code == 401) {
            AppLogger.w("401 Unauthorized received for CustomerDetailsPlain, force refreshing OAuth token & retrying...")
            val tokenRefresh = getBankAccessToken(forceRefresh = true)
            if (tokenRefresh is NetworkResult.Success) {
                return safeApiCall {
                    bankApiService.getCustomerDetailsPlain(
                        token = ApiConstants.getFormattedBankBearerToken(),
                        request = request
                    )
                }
            }
        }

        return result
    }


    /**
     * Validate Customer API Call
     * Endpoint: POST pg/api/v1/debitcard/validateCustomer
     */
    suspend fun validateCustomer(
        panNumber: String,
        accountNumber: String,
        requestId: String = ApiConstants.BANK_REQUEST_ID
    ): NetworkResult<ValidateCustomerResponse> {

        val request = ValidateCustomerRequest(
            panNumber = panNumber,
            accountNumber = accountNumber,
            requestId = requestId
        )

        AppLogger.i("Executing validateCustomer API for Account: $accountNumber | RequestId: $requestId")

        return safeApiCall {
            apiService.validateCustomer(request = request)
        }
    }

    /**
     * Generate OTP API Call
     * Endpoint: POST pg/api/v1/debitcard/generateOtp
     */
    suspend fun generateOtp(
        mobileNumber: String
    ): NetworkResult<GenerateOtpResponse> {
        val request = GenerateOtpRequest(
            mobileNumber = mobileNumber
        )

        AppLogger.i("Executing generateOtp API for Mobile: $mobileNumber")

        return safeApiCall {
            apiService.generateOtp(request = request)
        }
    }

    /**
     * Verify OTP API Call
     * Endpoint: POST pg/api/v1/debitcard/verify-otp
     */
    suspend fun verifyOtp(
        mobileNumber: String,
        otp: String
    ): NetworkResult<VerifyOtpResponse> {
        val request = VerifyOtpRequest(
            mobileNumber = mobileNumber,
            otp = otp
        )

        AppLogger.i("Executing verifyOtp API for Mobile: $mobileNumber | OTP: $otp")

        return safeApiCall {
            apiService.verifyOtp(request = request)
        }
    }

    /**
     * Link Card API Call
     * Endpoint: POST pg/api/v1/debitcard/linkCard
     */
    suspend fun linkCard(
        accountNumber: String,
        cardNumber: String,
        cardVariant: String,
        customerName: String
    ): NetworkResult<LinkCardResponse> {
        val request = LinkCardRequest(
            accountNumber = accountNumber,
            cardNumber = cardNumber,
            cardVariant = cardVariant,
            customerName = customerName
        )


        AppLogger.i("Executing linkCard API for Account: $accountNumber | Variant: $cardVariant")

        return safeApiCall {
            apiService.linkCard(request = request)
        }
    }

    /**
     * Generic Safe API Call Helper with full logging and error handling
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            val code = response.code()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                AppLogger.d("API Call Success [HTTP $code]: $body")
                android.util.Log.d("PNB_API_LOG", "✅ API SUCCESS [HTTP $code]: $body")
                NetworkResult.Success(body)
            } else {
                val errorRaw = response.errorBody()?.string()
                AppLogger.w("API Call Failed [HTTP $code] | ErrorBody: $errorRaw")
                android.util.Log.e("PNB_API_LOG", "❌ API FAILED [HTTP $code]: $errorRaw")

                val errorMessage = try {
                    if (!errorRaw.isNull_or_Empty()) {
                        val parsedError = gson.fromJson(errorRaw, ApiErrorResponse::class.java)
                        parsedError.responseMessage ?: parsedError.message ?: "HTTP $code Error"
                    } else {
                        "HTTP $code Error"
                    }
                } catch (e: Exception) {
                    "HTTP $code: ${response.message()}"
                }

                NetworkResult.Error(code = code, message = errorMessage)
            }
        } catch (e: Exception) {
            AppLogger.e("Network Exception during API Call", e)
            android.util.Log.e("PNB_API_LOG", "💥 NETWORK EXCEPTION: ${e.localizedMessage}", e)
            NetworkResult.Error(
                code = null,
                message = e.localizedMessage ?: "Network connection error",
                exception = e
            )
        }

    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.trim().isEmpty()
