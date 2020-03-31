package com.github.pidsamhai.gitrelease

import android.util.Log
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

internal fun validateApk(apk: File, md5: File): Boolean {
    val apkMd5 = apk.md5()
    val validMd5 = md5.readMd5()
    Log.e("Apk", apkMd5 )
    Log.e("MD5", validMd5 )
    if (apkMd5.trim() == validMd5.trim())
        return true
    return false

}

private fun File.md5(): String {
    val inputStream = FileInputStream(this)
    return try {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        while (true) {
            val readNum = inputStream.read(buf)
            if (readNum == -1) break
            outputStream.write(buf, 0, readNum)
        }
        outputStream.toHexString()
    } catch (e: Exception) {
        ""
    }
}

private fun File.readMd5(): String {
    return try {
        val bufferedReader: BufferedReader = this.bufferedReader()
        return bufferedReader.use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
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

private fun ByteArrayOutputStream.toHexString(): String {
    return try {
        val digest: MessageDigest = MessageDigest.getInstance("MD5")
        val hash: ByteArray = digest.digest(this.toByteArray())
        hash.toHexString()
    } catch (e: Exception) {
        ""
    }
}

