package com.github.pidsamhai.gitrelease.api

import android.content.ContentValues
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import java.io.File

internal class GithubReleaseRepository(
    owner: String,
    repo: String,
    private val currentVersion: String
) {

    private val repositoryUrl: String = "https://api.github.com/repos/$owner/$repo/releases"
    lateinit var downloadRequest: CancellableRequest

    suspend fun getReleaseVersion(): GetReleaseResult {
        return try {
            val (req, _, resObj) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            Log.i(ContentValues.TAG, "getRelease: $resObj")
            val assets = resObj[0].assets!![0]
            val checksum = resObj[0].assets!![1]
            val baseObj = resObj[0]
            GetReleaseResult.Success(
                apkName = assets!!.name!!,
                downloadUrl = assets.browserDownloadUrl!!,
                version = baseObj.tagName!!,
                size = (assets.size!! / 1024) / 1024,
                changeLog = baseObj.body!!,
                checksumUrl = checksum!!.browserDownloadUrl!!,
                newVersion = currentVersion != baseObj.tagName,
                checksumName = checksum.name!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
            GetReleaseResult.Error(e)
        }
    }

    fun downloadFile(
        fileName: String,
        url: String,
        basePath: File,
        onDownloadListener: OnDownloadListener
    ) {
        try {
            val filePath = File(basePath, fileName)
            downloadRequest = Fuel.download(url)
                .fileDestination { _, _ ->
                    filePath
                }
                .progress { readByte, totalByte ->
                    val percent = (readByte.toFloat() / totalByte.toFloat() * 100).toInt()
                    onDownloadListener.onProgress(percent, totalByte)
                }
                .response { _, _, _ ->
                    onDownloadListener.onSuccess(filePath)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadListener.onError(e)
        }

    }
}

internal sealed class GetReleaseResult {
    data class Success(
        val apkName: String,
        val version: String,
        val downloadUrl: String,
        val size: Long,
        val changeLog: String,
        val checksumUrl: String,
        val checksumName: String,
        val newVersion: Boolean
    ) : GetReleaseResult()

    data class Error(
        val error: Exception
    ) : GetReleaseResult()
}

internal interface OnDownloadListener {
    fun onError(e: Exception)
    fun onSuccess(filePath: File)
    fun onProgress(percent: Int, total: Long)
}
