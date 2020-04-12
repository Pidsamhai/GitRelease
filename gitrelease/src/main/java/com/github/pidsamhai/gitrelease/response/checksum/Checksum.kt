package com.github.pidsamhai.gitrelease.response.checksum


import com.google.gson.annotations.SerializedName

data class Checksum(
    @SerializedName("algorithm")
    val algorithm: String?,
    @SerializedName("type")
    val type: Type?
)