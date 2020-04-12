package com.github.pidsamhai.gitrelease.util

import com.github.pidsamhai.gitrelease.response.checksum.Checksum
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

internal fun validateApk(apk: File, md5: File): Boolean {
    when (val validMd5 = md5.toCheckSum()) {
        is Checksum -> {
            val apkMd5 = validMd5.algorithm?.let { apk.md5(it) } ?: ""
            return when (validMd5.algorithm) {
                "md5" -> {
                    apkMd5.trim() == validMd5.type?.md5 ?: ""
                }
                "sha1" -> {
                    apkMd5.trim() == validMd5.type?.sha1 ?: ""
                }
                "sha256" -> {
                    apkMd5.trim() == validMd5.type?.sha256 ?: ""
                }
                else -> {
                    false
                }
            }
        }
        is Error -> {
            return false
        }
        else -> {
            return false
        }
    }
}

private fun File.md5(algorithm: String): String {
    val inputStream = FileInputStream(this)
    return try {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        while (true) {
            val readNum = inputStream.read(buf)
            if (readNum == -1) break
            outputStream.write(buf, 0, readNum)
        }
        outputStream.toHexString(algorithm)
    } catch (e: Exception) {
        ""
    }
}


private fun File.toCheckSum(): Any {
    return try {
        val bufferReader: BufferedReader = this.bufferedReader()
        Gson().fromJson(bufferReader.use { it.readText() }, Checksum::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        e
    }
}


private fun ByteArray.toHexString(): String {
    val hexString: StringBuilder = StringBuilder()
    for (messageDigest: Byte in this) {
        var digits: String = Integer.toHexString(0xFF and messageDigest.toInt())
        while (digits.length < 2)
            digits = "0$digits"
        hexString.append(digits)
    }
    return hexString.toString()
}

private fun ByteArrayOutputStream.toHexString(algorithm: String): String {
    return try {
        val digest: MessageDigest = MessageDigest.getInstance(algorithm)
        val hash: ByteArray = digest.digest(this.toByteArray())
        hash.toHexString()
    } catch (e: Exception) {
        ""
    }
}

