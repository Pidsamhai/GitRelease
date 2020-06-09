package com.github.pidsamhai.gitrelease.response.checksum


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Checksum(
    @SerializedName("algorithm")
    val algorithm: String?,
    @SerializedName("type")
    val type: Type?
)