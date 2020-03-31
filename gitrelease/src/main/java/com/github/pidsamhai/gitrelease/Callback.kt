package com.github.pidsamhai.gitrelease

import com.github.pidsamhai.gitrelease.response.GitReleaseResponse
import java.io.File

typealias IsError = Boolean
typealias ReleaseResponseData = Pair<ResponseData, IsError>
typealias ReleaseDataCallback = (data: ReleaseResponseData) -> Unit
typealias DownloadCallback = (percent: Int, current: Long, fileSize: Long, success: Boolean, apk: File) -> Unit


data class UpdateData(
    val apkName: String? = null,
    val version: String? = null,
    val downloadUrl: String? = null,
    val size: Long? = null,
    val changeLog: String? = null,
    val checksumUrl: String? = null,
    val checksumName: String? = "checksum.md5",
    val err: Exception? = null,
    val newVersion: Boolean = false
)

data class Checksum(
    val md5: String?,
    val sha1: String?,
    val sha512: String?
)

data class ResponseData(
    @Suppress("ArrayInDataClass")
    val response: Array<GitReleaseResponse>?,
    val err: Exception?
)