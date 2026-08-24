package com.pnb.bank.data.api.bankgateway

import com.google.gson.GsonBuilder
import com.pnb.bank.data.api.ApiConstants
import com.pnb.bank.utils.AppLogger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object BankApiClient {

    private const val TAG_BANK_API = "BANK_API_CLIENT"

    // Custom Logging Interceptor routed to AppLogger & Logcat PNB_API_LOG
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            AppLogger.d("[$TAG_BANK_API] $message")
            android.util.Log.d("PNB_API_LOG", "[$TAG_BANK_API] $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Dynamic Header Interceptor for Bank APIs reading dynamic bankAccessToken
    private val headerInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.VALUE_APPLICATION_JSON)
                .header(ApiConstants.HEADER_ACCEPT, ApiConstants.VALUE_APPLICATION_JSON)

            // Inject Bank Bearer Token dynamically if bankAccessToken exists and not explicitly overridden
            if (originalRequest.header(ApiConstants.HEADER_AUTHORIZATION) == null && ApiConstants.bankAccessToken.isNotEmpty()) {
                requestBuilder.header(ApiConstants.HEADER_AUTHORIZATION, ApiConstants.getFormattedBankBearerToken())
            }

            chain.proceed(requestBuilder.build())
        }
    }

    // Unsafe TrustManager builder for SIT/Testing SSL handshake environments
    private fun getUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            AppLogger.e("Failed to create Unsafe SSL OkHttpClient for BankApiClient", e)
            OkHttpClient.Builder()
        }
    }

    // OkHttpClient Setup with 30s timeouts & SSL Bypass
    private val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClientBuilder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Gson Configuration
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Retrofit Instance for Bank APIs
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Expose BankApiService client
    val apiService: BankApiService by lazy {
        retrofit.create(BankApiService::class.java)
    }
}
