package com.github.pidsamhai.gitrelease.response

import java.io.File

typealias IsError = Boolean
typealias ReleaseResponseData = Pair<ResponseData, IsError>
typealias DownloadCallback = (percent: Int, current: Long, fileSize: Long, success: Boolean, apk: File) -> Unit


data class UpdateData(
    val apkName: String? = null,
    val version: String? = null,
    val downloadUrl: String? = null,
    val size: Long? = null,
    val changeLog: String? = null,
    val err: Exception? = null
)