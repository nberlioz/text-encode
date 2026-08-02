package com.example.cryptoapp

import android.graphics.Bitmap
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
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
     * Chiffre le texte et génère directement un QR Code (Bitmap) à partir de la chaîne Base64 résultante.
     * 
     * @param plainText Le texte en clair à chiffrer.
     * @param passphrase La clé/mot de passe.
     * @param qrSize La dimension en pixels du QR Code (par défaut 500x500px).
     * @return Le Bitmap du QR Code, ou null en cas d'erreur de génération.
     */
    fun encryptToQrCode(plainText: String, passphrase: String, qrSize: Int = 500): Bitmap? {
        val encryptedBase64 = encrypt(plainText, passphrase)
        return generateQrCode(encryptedBase64, qrSize)
    }

    /**
     * Génère une image Bitmap (QR Code) à partir d'une chaîne de caractères.
     */
    fun generateQrCode(content: String, size: Int = 500): Bitmap? {
        val writer = MultiFormatWriter()
        return try {
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val encoder = BarcodeEncoder()
            encoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
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