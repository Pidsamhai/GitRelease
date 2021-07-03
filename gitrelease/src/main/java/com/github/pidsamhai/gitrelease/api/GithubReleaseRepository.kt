package com.github.pidsamhai.gitrelease.api

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.exceptions.*
import com.github.pidsamhai.gitrelease.response.Response
import com.github.pidsamhai.gitrelease.response.checksum.Checksum
import com.github.pidsamhai.gitrelease.response.github.Asset
import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import com.github.pidsamhai.gitrelease.util.reGexApkFileType
import com.github.pidsamhai.gitrelease.util.reGexCheckSumFile
import com.github.pidsamhai.gitrelease.util.validateApk
import com.vdurmont.semver4j.Semver
import java.io.File

internal class GithubReleaseRepository private constructor(val config: GitRelease.Config) {

    private val repositoryUrl: String =
        "https://api.github.com/repos/${config.owner}/${config.repo}/releases"
    lateinit var downloadRequest: CancellableRequest

    suspend fun checkNewVersion(): Response<NewVersion> {
        return try {
            val (_, _, result) = repositoryUrl
                .httpGet()
                .awaitObjectResponse(
                    gsonDeserializerOf(
                        Array<GitReleaseResponse>::class.java
                    )
                )
            when {
                !result.isNullOrEmpty() -> {
                    val latestRelease: GitReleaseResponse = result[0]
                    val apkAsset: Asset? = latestRelease.assets?.find {
                        reGexApkFileType.find(it?.name ?: return@find false) != null
                    }
                    val checksumAsset: Asset? = latestRelease.assets?.find {
                        reGexCheckSumFile.matches(it?.name ?: return@find false)
                    }
                    when {
                        (checksumAsset != null && apkAsset != null) -> {
                            val newVersion = NewVersion(
                                apkName = apkAsset.name ?: "",
                                downloadUrl = apkAsset.browserDownloadUrl ?: "",
                                version = latestRelease.tagName ?: "",
                                size = (apkAsset.size ?: 1 / 1024) / 1024,
                                changeLog = latestRelease.body ?: "",
                                checksumUrl = checksumAsset.browserDownloadUrl ?: "",
                                checksumName = checksumAsset.name ?: ""
                            )
                            when {
                                isNewVersion(latestRelease.tagName) -> {
                                    Response.Success(newVersion)
                                }
                                else -> {
                                    Response.Error(NoNewVersionAvailableException())
                                }
                            }
                        }
                        else -> Response.Error(NoFileAssetsException(latestRelease.body))
                    }
                }
                else -> Response.Error(NoReleaseAvailableException())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(CheckVersionException())
        }
    }

    suspend fun checksum(url: String, apk: File): Response<Boolean> {
        return try {
            val (_, _, result) = url
                .httpGet()
                .awaitObjectResponse(
                    gsonDeserializerOf(
                        Checksum::class.java
                    )
                )
            Response.Success(validateApk(apk, result))
        } catch (err: Exception) {
            Response.Error(ChecksumException())
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
                .response { _, _, result ->
                    when {
                        result.component1() != null -> {
                            onDownloadListener.onSuccess(filePath)
                        }
                        result.component2() != null -> {
                            onDownloadListener.onError(Exception(result.component2()?.message))
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadListener.onError(e)
        }

    }

    private fun isNewVersion(version: String?): Boolean =
        Semver(version).isGreaterThan(config.currentVersion)

    companion object {
        @Volatile private var instance: GithubReleaseRepository? = null
        fun getInstance(config: GitRelease.Config): GithubReleaseRepository {
            return instance ?: synchronized(this) {
                return instance ?: GithubReleaseRepository(config)
            }
        }
    }
}

internal data class NewVersion(
    val apkName: String,
    val version: String,
    val downloadUrl: String,
    val size: Long,
    val changeLog: String,
    val checksumUrl: String,
    val checksumName: String
)

internal interface OnDownloadListener {
    fun onError(e: Exception)
    fun onSuccess(filePath: File)
    fun onProgress(percent: Int, total: Long)
}
