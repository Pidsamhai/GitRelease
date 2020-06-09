package com.github.pidsamhai.gitrelease.response.github


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Asset(
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("size")
    val size: Long?
)