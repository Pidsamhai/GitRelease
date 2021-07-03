package com.github.pidsamhai.gitrelease.response

internal sealed class Response<out T : Any> {
    data class Success<out T : Any>(val value: T) : Response<T>()
    data class Error(val err: Exception) : Response<Nothing>()
}