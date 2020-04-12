package com.github.pidsamhai.gitrelease

import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import java.io.File

internal typealias IsError = Boolean
internal typealias ReleaseResponseData = Pair<ResponseData, IsError>
internal typealias ReleaseDataCallback = (data: ReleaseResponseData) -> Unit
internal typealias DownloadCallback = (percent: Int, current: Long, fileSize: Long, success: Boolean, apk: File) -> Unit


internal data class UpdateData(
    val apkName: String? = null,
    val version: String? = null,
    val downloadUrl: String? = null,
    val size: Long? = null,
    val changeLog: String? = null,
    val checksumUrl: String? = null,
    val checksumName: String? = null,
    val err: Exception? = null,
    val newVersion: Boolean = false
)

internal data class ResponseData(
    @Suppress("ArrayInDataClass")
    val response: Array<GitReleaseResponse>?,
    val err: Exception?
)