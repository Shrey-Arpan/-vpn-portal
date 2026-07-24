package com.example.totp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpManager {

    /**
     * Decode a hex string into a byte array.
     */
    fun decodeHex(hex: String): ByteArray {
        val clean = hex.replace("\\s+".toRegex(), "").lowercase()
        if (clean.isEmpty()) return byteArrayOf()
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val j = clean.substring(index, index + 2).toInt(16)
            result[i] = j.toByte()
        }
        return result
    }

    /**
     * Decode a base32 string into a byte array.
     */
    fun decodeBase32(base32: String): ByteArray {
        val clean = base32.replace("=", "").replace("\\s+".toRegex(), "").uppercase()
        if (clean.isEmpty()) return byteArrayOf()
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        var bits = 0
        var value = 0
        val bytes = mutableListOf<Byte>()

        for (c in clean) {
            val charValue = base32Chars.indexOf(c)
            if (charValue < 0) continue
            value = (value shl 5) or charValue
            bits += 5
            if (bits >= 8) {
                bytes.add(((value shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }
        return bytes.toByteArray()
    }

    /**
     * Generate 6-digit TOTP token using HMAC-SHA1 RFC 6238.
     * @param secretInput Hex string or Base32 secret string
     * @param periodSeconds Timestep in seconds (default 20s as specified for Sophos XG VPN)
     */
    fun generateTotp(
        secretInput: String,
        periodSeconds: Long = 20L,
        numDigits: Int = 6,
        timeMillis: Long = System.currentTimeMillis()
    ): String {
        return try {
            val cleanInput = secretInput.trim()
            val keyBytes = if (cleanInput.length == 32 && cleanInput.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                try { decodeHex(cleanInput) } catch (e: Exception) { decodeBase32(cleanInput) }
            } else {
                try { decodeBase32(cleanInput) } catch (e: Exception) { decodeHex(cleanInput) }
            }

            if (keyBytes.isEmpty()) return "000000"

            val counter = (timeMillis / 1000L) / periodSeconds
            val data = ByteBuffer.allocate(8).putLong(counter).array()

            val mac = Mac.getInstance("HmacSHA1")
            val signKey = SecretKeySpec(keyBytes, "HmacSHA1")
            mac.init(signKey)
            val hash = mac.doFinal(data)

            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val otp = binary % 10.toDouble().pow(numDigits.toDouble()).toLong()
            otp.toString().padStart(numDigits, '0')
        } catch (e: Exception) {
            "000000"
        }
    }

    /**
     * Seconds remaining in current timestep window (1 to periodSeconds)
     */
    fun getSecondsRemaining(periodSeconds: Long = 20L, timeMillis: Long = System.currentTimeMillis()): Int {
        val currentSecondInWindow = (timeMillis / 1000L) % periodSeconds
        val remaining = (periodSeconds - currentSecondInWindow).toInt()
        return if (remaining <= 0) periodSeconds.toInt() else remaining
    }

    /**
     * Generate complete dynamic password formatted as: basePassword + TOTP
     * Example: Qwerty@1234 + 123456 -> Qwerty@1234123456
     */
    fun buildDynamicPassword(
        basePassword: String = "Qwerty@1234",
        secretInput: String = "e5bd156e098db764fb77816ebdc216ce",
        periodSeconds: Long = 20L
    ): String {
        val totp = generateTotp(secretInput = secretInput, periodSeconds = periodSeconds)
        return basePassword + totp
    }
}
