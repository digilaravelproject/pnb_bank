package com.pnb.bank

import com.pnb.bank.utils.BankCryptoUtils
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDecryption() {
        val sampleEncResp = "fSeRKXZBAj7F279aZBjI3P5JNOOHrFObFN8u2J48UF98rfrIS/wymsNkXahhftgIfYXKs5VM7neCQYYybLQMNLdIO53gHbMHM1JIURC0dSo="
        val decrypted = BankCryptoUtils.decrypt(sampleEncResp)
        println("DECRYPTED TEST RESULT: $decrypted")
        assertTrue(decrypted.contains("status") || decrypted.contains("NO DATA FOUND") || decrypted.contains("Success"))
    }
}

