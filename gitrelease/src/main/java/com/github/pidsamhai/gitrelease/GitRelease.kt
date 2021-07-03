package com.github.pidsamhai.gitrelease

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.exceptions.NoFileAssetsException
import com.github.pidsamhai.gitrelease.exceptions.NoNewVersionAvailableException
import com.github.pidsamhai.gitrelease.exceptions.NoReleaseAvailableException
import com.github.pidsamhai.gitrelease.response.Response
import com.github.pidsamhai.gitrelease.ui.GitReleaseDialog
import java.io.File

class GitRelease(private val activity: AppCompatActivity,config: Config) {

    private val repository = GithubReleaseRepository.getInstance(config)
    private val releaseDialog = GitReleaseDialog(repository)

    fun checkUpdate(listener: OnCheckReleaseListener? = null) {
        releaseDialog.newVersion = null
        releaseDialog.listener = listener
        releaseDialog.show(activity.supportFragmentManager, activity.javaClass.simpleName)
    }

    suspend fun checkUpdateNoLoading(listener: OnCheckReleaseListener? = null) {
        when(val res = repository.checkNewVersion()) {
            is Response.Error -> {
                Log.i("TAG", "Error: ${res.err}")
                when (res.err) {
                    is NoFileAssetsException -> {
                        listener?.onError()
                    }
                    is NoReleaseAvailableException -> {
                        listener?.onCompleteNoUpdateFound()
                    }
                    is NoNewVersionAvailableException -> {
                        listener?.onCompleteLatestVersion()
                    }
                    else -> {
                        listener?.onError()
                    }
                }
            }
            is Response.Success -> {
                releaseDialog.listener = listener
                releaseDialog.newVersion = res.value
                releaseDialog.show(activity.supportFragmentManager, activity.javaClass.simpleName)
            }
        }
    }

    interface OnCheckReleaseListener {
        fun onCompleteNoUpdateFound() = Unit
        fun onCancelCheckUpdate() = Unit
        fun onError() = Unit
        fun onDownloadCancel() = Unit
        fun onUpdateCancel() = Unit
        fun onDownloadComplete(apk: File) = Unit
        fun onChecksumError() = Unit
        fun onCompleteLatestVersion() = Unit
    }

    data class Config(
        val owner: String,
        val repo: String,
        val currentVersion: String,
        val checksum: Boolean = true
    )

    companion object {
        fun installApk(activity: Activity, apk: File) {
            com.github.pidsamhai.gitrelease.util.installApk(activity, apk)
        }
    }
}
