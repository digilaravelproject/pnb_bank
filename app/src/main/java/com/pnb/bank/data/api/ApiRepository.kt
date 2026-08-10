package com.pnb.bank.data.api

import com.google.gson.Gson
import com.pnb.bank.data.api.models.ApiErrorResponse
import com.pnb.bank.data.api.models.ValidateCustomerRequest
import com.pnb.bank.data.api.models.ValidateCustomerResponse
import com.pnb.bank.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ApiRepository(private val apiService: ApiService = ApiClient.apiService) {

    private val gson = Gson()

    /**
     * Validate Customer API Call
     * Endpoint: POST pg/api/v1/debitcard/validateCustomer
     */
    suspend fun validateCustomer(
        panNumber: String,
        accountNumber: String,
        requestId: String = ApiConstants.activeRequestId
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
    ): NetworkResult<com.pnb.bank.data.api.models.GenerateOtpResponse> {
        val request = com.pnb.bank.data.api.models.GenerateOtpRequest(
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
    ): NetworkResult<com.pnb.bank.data.api.models.VerifyOtpResponse> {
        val request = com.pnb.bank.data.api.models.VerifyOtpRequest(
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
    ): NetworkResult<com.pnb.bank.data.api.models.LinkCardResponse> {
        val request = com.pnb.bank.data.api.models.LinkCardRequest(
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
                NetworkResult.Success(body)
            } else {
                val errorRaw = response.errorBody()?.string()
                AppLogger.w("API Call Failed [HTTP $code] | ErrorBody: $errorRaw")

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
            NetworkResult.Error(
                code = null,
                message = e.localizedMessage ?: "Network connection error",
                exception = e
            )
        }
    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.trim().isEmpty()
