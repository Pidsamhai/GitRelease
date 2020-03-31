package com.github.pidsamhai.gitrelease.response


import com.google.gson.annotations.SerializedName

internal data class Asset(
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String?,
    @SerializedName("content_type")
    val contentType: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("download_count")
    val downloadCount: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("label")
    val label: Any?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("node_id")
    val nodeId: String?,
    @SerializedName("size")
    val size: Long?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("uploader")
    val uploader: Uploader?,
    @SerializedName("url")
    val url: String?
)