package com.github.pidsamhai.gitrelease.response

data class ResponseData(
    @Suppress("ArrayInDataClass")
    val data: Array<GitReleaseResponse>?,
    val err: Exception?
)