package com.github.pidsamhai.gitrelease.response

import java.io.Serializable


typealias NewVersion = Boolean
typealias IsError = Boolean
typealias ReleaseResponse = ReleaseResponseOf<ResponseData, IsError, NewVersion>

data class ReleaseResponseOf<out A, out B, out C>(
    val data: A,
    val isError: B,
    val isNew: C
) : Serializable