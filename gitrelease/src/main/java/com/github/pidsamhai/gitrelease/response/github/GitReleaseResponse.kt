package com.github.pidsamhai.gitrelease.response.github


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class GitReleaseResponse(
    @SerializedName("assets")
    val assets: List<Asset?>?,
    @SerializedName("assets_url")
    val assetsUrl: String?,
    @SerializedName("body")
    val body: String?,
    @SerializedName("tag_name")
    val tagName: String?
)