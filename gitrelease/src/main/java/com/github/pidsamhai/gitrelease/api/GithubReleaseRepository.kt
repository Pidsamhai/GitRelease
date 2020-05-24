package com.github.pidsamhai.gitrelease.api

import android.content.ContentValues
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.pidsamhai.gitrelease.response.github.Asset
import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import com.github.pidsamhai.gitrelease.util.reGexApkFileType
import com.github.pidsamhai.gitrelease.util.reGexCheckSumFile
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
            val (_, _, resObj) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            Log.i(ContentValues.TAG, "getRelease: $resObj")
            val baseObj: GitReleaseResponse = resObj[0]
            val apkAsset: Asset? = baseObj.assets?.find {
                reGexApkFileType.find(it?.name ?: "") != null
            }
            val checksumAsset: Asset? = baseObj.assets?.find {
                reGexCheckSumFile.matches(it?.name ?: "")
            }
            val isNew = currentVersion != baseObj.tagName
            if (!isNew) {
                GetReleaseResult.SuccessLatestVersion
            } else if (apkAsset != null && checksumAsset != null && isNew) {
                GetReleaseResult.SuccessNewVersion(
                    apkName = apkAsset.name!!,
                    downloadUrl = apkAsset.browserDownloadUrl!!,
                    version = baseObj.tagName!!,
                    size = (apkAsset.size!! / 1024) / 1024,
                    changeLog = baseObj.body!!,
                    checksumUrl = checksumAsset.browserDownloadUrl!!,
                    checksumName = checksumAsset.name!!
                )
            } else {
                GetReleaseResult.Error(Exception("No file Assets"))
            }

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
    data class SuccessNewVersion(
        val apkName: String,
        val version: String,
        val downloadUrl: String,
        val size: Long,
        val changeLog: String,
        val checksumUrl: String,
        val checksumName: String
    ) : GetReleaseResult()

    data class Error(
        val error: Exception
    ) : GetReleaseResult()

    object SuccessLatestVersion : GetReleaseResult()
}

internal interface OnDownloadListener {
    fun onError(e: Exception)
    fun onSuccess(filePath: File)
    fun onProgress(percent: Int, total: Long)
}
