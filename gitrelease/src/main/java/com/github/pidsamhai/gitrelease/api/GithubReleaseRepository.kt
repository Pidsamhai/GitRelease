package com.github.pidsamhai.gitrelease.api

import android.content.ContentValues
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.pidsamhai.gitrelease.*
import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import java.io.File

internal class GithubReleaseRepository(
    owner: String,
    repo: String,
    private val currentVersion: String
) {

    private val repositoryUrl: String = "https://api.github.com/repos/$owner/$repo/releases"
    lateinit var downloadRequest: CancellableRequest

    suspend fun awaitGetReleaseData(): ReleaseResponseData {
        return try {
            val (_, _, data) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            ReleaseResponseData(
                ResponseData(
                    data,
                    null
                ), false
            )
        } catch (e: Exception) {
            ReleaseResponseData(
                ResponseData(
                    null,
                    e
                ), true
            )
        }
    }

    fun getReleaseData(callBack: ReleaseDataCallback) {
        repositoryUrl.httpGet()
            .responseObject(gsonDeserializerOf(Array<GitReleaseResponse>::class.java)) { result ->
                when (result) {
                    is Result.Success -> {
                        callBack(ReleaseResponseData(ResponseData(result.get(), null), false))
                    }
                    is Result.Failure -> {
                        callBack(ReleaseResponseData(ResponseData(null, result.error), false))
                    }
                }

            }

    }

    suspend fun getReleaseVersion(): UpdateData {
        print(repositoryUrl)
        return try {
            val (_, _, resObj) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            Log.i(ContentValues.TAG, "getRelease: $resObj")
            val assets = resObj[0].assets!![0]
            val checksum = resObj[0].assets!![1]
            val baseObj = resObj[0]
            UpdateData(
                apkName = assets!!.name!!,
                downloadUrl = assets.browserDownloadUrl!!,
                version = baseObj.tagName,
                size = (assets.size!! / 1024) / 1024,
                changeLog = baseObj.body,
                checksumUrl = checksum!!.browserDownloadUrl,
                newVersion = currentVersion != baseObj.tagName,
                checksumName = checksum.name
            )
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateData(err = e)
        }
    }

    fun downloadFile(fileName: String, url: String, basePath: File, callBack: DownloadCallback) {
        val filePath = File(basePath, fileName)
        downloadRequest = Fuel.download(url)
            .fileDestination { _, _ ->
                filePath
            }
            .progress { readByte, totalByte ->
                val percent = (readByte.toFloat() / totalByte.toFloat() * 100).toInt()
                callBack(percent, readByte, totalByte, false, filePath)
            }
            .response { _, _, _ ->
                callBack(0, 0, 0, true, filePath)
            }
    }
}
