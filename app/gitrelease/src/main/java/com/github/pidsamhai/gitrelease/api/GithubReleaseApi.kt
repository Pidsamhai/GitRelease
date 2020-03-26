package com.github.pidsamhai.gitrelease.api

import android.content.ContentValues
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.pidsamhai.gitrelease.response.*
import java.io.File

class GithubReleaseApi(
    owner: String,
    repo: String,
    private val currentVersion: String
) {

    private val repositoryUrl: String = "https://api.github.com/repos/$owner/$repo/releases"
    private var isNewVersion: Boolean = false

    suspend fun getReleaseData(): ReleaseResponseData {
        return try {
            val (_, _, data) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            ReleaseResponseData(ResponseData(data, null), false)
        } catch (e: Exception) {
            ReleaseResponseData(ResponseData(null, e), true)
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
            UpdateData(
                apkName = assets!!.name!!,
                downloadUrl = assets.browserDownloadUrl!!,
                version = resObj[0].tagName,
                size = (assets.size!! / 1024) / 1024,
                changeLog = resObj[0].body
            )
        } catch (e: Exception) {
            UpdateData(err = e)
        }
    }

    fun downloadUpdateApk(apkName: String, url: String, path: File, callBack: DownloadCallback) {
        val apkPath = File(path, apkName)
        Fuel.download(url)
            .fileDestination { _, _ ->
                apkPath
            }
            .progress { readByte, totalByte ->
                val percent = (readByte.toFloat() / totalByte.toFloat() * 100).toInt()
                callBack(percent, readByte, totalByte, false, apkPath)
            }
            .response { _, _, _ ->
                callBack(0, 0, 0, true, apkPath)
            }
    }

}