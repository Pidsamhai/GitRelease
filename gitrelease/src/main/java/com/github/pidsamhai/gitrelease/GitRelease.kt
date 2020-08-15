package com.github.pidsamhai.gitrelease

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.ui.GitReleaseDialog
import java.io.File

class GitRelease(private val activity: AppCompatActivity,config: Config, listener: OnCheckReleaseListener) {

    private val releaseDialog = GitReleaseDialog(config, listener)

    fun checkUpdate() {
        releaseDialog.show(activity.supportFragmentManager, activity.javaClass.simpleName)
    }

    interface OnCheckReleaseListener {
        fun onCompleteNoUpdateFound()
        fun onCancelCheckUpdate()
        fun onError()
        fun onDownloadCancel()
        fun onUpdateCancel()
        fun onDownloadError()
        fun onDownloadComplete(apk: File)
        fun onChecksumError()
        fun onCompleteLatestVersion()
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
