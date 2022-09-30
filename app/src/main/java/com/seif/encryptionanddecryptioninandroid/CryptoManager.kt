package com.seif.encryptionanddecryptioninandroid

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {
    // getting access to teh keystore
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    // iv(initialization vector): is what describes the initial state of our encryption, generated when we encrypt something
    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        // alias: what is used to ask the keystore for our key (as key in sharedPreference)
        val existingKey = keyStore.getEntry("secret", null) as KeyStore.SecretKeyEntry?
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
//        return KeyGenerator.getInstance(ALGORITHM).generateKey()
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    "secret",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false) // related to biometeric prompts (if we use finger print in our app and we want to say that this key can only be accessed if the user is authenticated
                    .setRandomizedEncryptionRequired(true) // randomize the encryption
                    .build()
            )
        }.generateKey()
    }
    // byteArray: the bytes we want to encrypt
    // outputStream: write result of the encryption that will be of type outputStream
    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val encryptedBytes: ByteArray = encryptCipher.doFinal(bytes)
        // we need to take this encryptedBytes array and put it in the outputStream, why?
        // bec: when we want to decrypt what we encrypted we need this iv value that was generated during encryption with our cipher
        // bec when we then decrypt that we need to pass this byte array "getDecryptCipherForIv(iv: ByteArray)"
        // a common way to do this is to append this iv in front of the actual encrypted bytes in our outputStream
        outputStream.use {
            it.write(encryptCipher.iv.size) // write size of our iv vector ( to know how many bytes we need to read that)
            it.write(encryptCipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            getDecryptCipherForIv(iv).doFinal(encryptedBytes) // decryption
        }
    }

    companion object {
        private const val ALGORITHM =
            KeyProperties.KEY_ALGORITHM_AES // AES (Symmetric Encryption Algorithm) available starts from android M
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC //
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION =
            "$ALGORITHM/$BLOCK_MODE/$PADDING" // will combine all of these values together
    }

}
// what we needed to encrypt and decrypt:
// 1) cipher: tells our app how it should encrypt decrypt our string
// 2) cryptoAlgorithm
// 3) block type and padding: some kind of form how the encryption should actually happen