package com.github.pidsamhai.gitrelease.response.checksum


import com.google.gson.annotations.SerializedName

data class Type(
    @SerializedName("md5")
    val md5: String?,
    @SerializedName("sha1")
    val sha1: String?,
    @SerializedName("sha256")
    val sha256: String
)