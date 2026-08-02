package com.example.cryptoapp

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_SIZE_BYTE = 12

    // Transforme un mot de passe utilisateur en clé AES-256 via SHA-256
    private fun deriveKey(passphrase: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(passphrase.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Chiffre le texte et retourne une chaîne Base64 contenant : [12 octets IV] + [Texte chiffré]
     */
    fun encrypt(plainText: String, passphrase: String): String {
        val secretKey = deriveKey(passphrase)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv // IV généré automatiquement par Android
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Fusion de l'IV et des données chiffrées
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Extrait l'IV du message et déchiffre le contenu.
     */
    fun decrypt(encryptedBase64: String, passphrase: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val secretKey = deriveKey(passphrase)

        // Extraction de l'IV
        val iv = ByteArray(IV_SIZE_BYTE)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)

        // Extraction du texte chiffré
        val encryptedSize = combined.size - IV_SIZE_BYTE
        val encryptedBytes = ByteArray(encryptedSize)
        System.arraycopy(combined, IV_SIZE_BYTE, encryptedBytes, 0, encryptedSize)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }
}
