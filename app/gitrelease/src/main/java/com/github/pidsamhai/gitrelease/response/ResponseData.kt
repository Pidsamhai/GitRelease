package com.github.pidsamhai.gitrelease.response

data class ResponseData(
    @Suppress("ArrayInDataClass")
    val response: Array<GitReleaseResponse>?,
    val err: Exception?
)