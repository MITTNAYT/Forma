package com.habitflow.app.core.crypto

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Zero-Knowledge Vault Manager for HabitFlow v2.
 * Encrypts and decrypts user database backups using AES-256-GCM with PBKDF2-HMAC-SHA256.
 * Zero data ever leaves the device unencrypted; passwords are never persisted.
 */
object CryptoVaultManager {

    private const val HEADER_MAGIC = "--- HABITFLOW ENCRYPTED VAULT V1 ---"
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    fun encrypt(plainText: String, passphrase: CharArray): String {
        val random = SecureRandom()

        // 1. Generate Salt
        val salt = ByteArray(SALT_LENGTH_BYTES)
        random.nextBytes(salt)

        // 2. Generate IV (12 bytes for GCM)
        val iv = ByteArray(IV_LENGTH_BYTES)
        random.nextBytes(iv)

        // 3. Derive AES-256 Key via PBKDF2-HMAC-SHA256
        val keySpec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = keyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        // 4. Encrypt with AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        // 5. Build envelope JSON
        val envelope = JSONObject().apply {
            put("version", 1)
            put("kdf", "PBKDF2-HMAC-SHA256")
            put("iterations", ITERATIONS)
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(cipherText, Base64.NO_WRAP))
            put("timestamp", System.currentTimeMillis())
        }

        return "$HEADER_MAGIC\n" + envelope.toString(2)
    }

    fun decrypt(encryptedEnvelope: String, passphrase: CharArray): Result<String> {
        return runCatching {
            val cleaned = encryptedEnvelope.trim()
            val jsonStr = if (cleaned.startsWith(HEADER_MAGIC)) {
                cleaned.removePrefix(HEADER_MAGIC).trim()
            } else {
                cleaned
            }

            val json = JSONObject(jsonStr)
            val iterations = json.optInt("iterations", ITERATIONS)
            val salt = Base64.decode(json.getString("salt"), Base64.NO_WRAP)
            val iv = Base64.decode(json.getString("iv"), Base64.NO_WRAP)
            val ciphertext = Base64.decode(json.getString("ciphertext"), Base64.NO_WRAP)

            // Derive key
            val keySpec = PBEKeySpec(passphrase, salt, iterations, KEY_LENGTH_BITS)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secretKeyBytes = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")

            // Decrypt
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(ciphertext)

            String(decryptedBytes, StandardCharsets.UTF_8)
        }
    }
}
