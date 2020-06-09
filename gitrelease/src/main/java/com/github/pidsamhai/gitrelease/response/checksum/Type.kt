package com.github.pidsamhai.gitrelease.response.checksum


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Type(
    @SerializedName("md5")
    val md5: String?,
    @SerializedName("sha1")
    val sha1: String?,
    @SerializedName("sha256")
    val sha256: String
)