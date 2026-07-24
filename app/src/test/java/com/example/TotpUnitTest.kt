package com.example

import com.example.totp.TotpManager
import org.junit.Assert.*
import org.junit.Test

class TotpUnitTest {

    @Test
    fun totp_generation_is_correct() {
        val hexSecret = "e5bd156e098db764fb77816ebdc216ce"
        val base32Secret = "4W6RK3QJRW3WJ63XQFXL3QQWZY"

        // Generate TOTP with 20s timestep
        val codeHex = TotpManager.generateTotp(hexSecret, periodSeconds = 20L)
        val codeBase32 = TotpManager.generateTotp(base32Secret, periodSeconds = 20L)

        // Both representations should yield identical 6-digit TOTP
        assertEquals(codeHex, codeBase32)
        assertEquals(6, codeHex.length)
        assertTrue(codeHex.all { it.isDigit() })

        // Dynamic password format
        val dynPass = TotpManager.buildDynamicPassword(
            basePassword = "Qwerty@1234",
            secretInput = hexSecret,
            periodSeconds = 20L
        )
        assertTrue(dynPass.startsWith("Qwerty@1234"))
        assertEquals(17, dynPass.length) // 11 + 6 = 17
    }
}
