package com.hieutm.booksplitting.service

import com.hieutm.booksplitting.configuration.ApplicationConfiguration
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import java.io.InputStream
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


@Service
class EncryptionDecryptionService(private val applicationConfiguration: ApplicationConfiguration) {

    fun encryptFileContent(inputStream: InputStream, size: Int): ByteArray {
        return doCrypto(Cipher.ENCRYPT_MODE, inputStream, size)
    }

    fun decryptFileContent(inputStream: InputStream, size: Int): ByteArray {
        return doCrypto(Cipher.DECRYPT_MODE, inputStream, size)
    }

    private fun doCrypto(cipherMode: Int, inputStream: InputStream, size: Int): ByteArray {
        try {
            Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
            val secretKey = SecretKeySpec(
                applicationConfiguration.secretKey!!.toByteArray(),
                applicationConfiguration.fileCryptoAlgorithm!!.key!!
            )
            val cipher = Cipher.getInstance(
                applicationConfiguration.fileCryptoAlgorithm!!.content!!,
                applicationConfiguration.securityProvider!!
            )
            cipher.init(cipherMode, secretKey)
            val inputBytes = ByteArray(size)
            inputStream.read(inputBytes)
            val outputBytes = cipher.doFinal(inputBytes)
            inputStream.close()
            return outputBytes
        } catch (ex: Exception) {
            throw Exception("Error during encrypting/decrypting file: $ex")
        }

    }

    fun encryptFileName(fileName: String, algorithm: String): String? {
        return when (algorithm) {
            "MD5" -> DigestUtils.md5Hex(fileName)
            "SHA256" -> DigestUtils.sha256Hex(fileName)
            else -> DigestUtils.sha384Hex(fileName)
        }
    }
}