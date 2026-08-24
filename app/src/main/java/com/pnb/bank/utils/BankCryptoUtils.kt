package com.pnb.bank.utils

import android.util.Base64
import com.pnb.bank.data.api.ApiConstants
import java.nio.charset.StandardCharsets
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object BankCryptoUtils {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ALGO_AES = "AES"
    private const val GCM_TAG_LENGTH = 16 // 16 bytes = 128 bits GCM Tag

    /**
     * Encrypts plaintext String using AES/GCM/NoPadding and returns Base64 String
     */
    fun encrypt(plainText: String, secretKeyStr: String = ApiConstants.BANK_ENCRYPTION_KEY): String {
        return try {
            val keyBytes = secretKeyStr.toByteArray(StandardCharsets.UTF_8)
            val iv = Arrays.copyOf(keyBytes, 16)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(keyBytes, ALGO_AES)
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            java.util.Base64.getEncoder().encodeToString(encryptedBytes)
        } catch (e: Exception) {
            AppLogger.e("AES/GCM Encryption Exception", e)
            throw e
        }
    }

    /**
     * Decrypts Base64 encrypted String using AES/GCM/NoPadding and returns plaintext String
     */
    fun decrypt(encryptedBase64: String, secretKeyStr: String = ApiConstants.BANK_ENCRYPTION_KEY): String {
        return try {
            val keyBytes = secretKeyStr.toByteArray(StandardCharsets.UTF_8)
            val iv = Arrays.copyOf(keyBytes, 16)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(keyBytes, ALGO_AES)
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)
            val decodedBytes = java.util.Base64.getDecoder().decode(encryptedBase64.trim())
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            AppLogger.e("AES/GCM Decryption Exception", e)
            throw e
        }
    }

}
