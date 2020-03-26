package com.github.pidsamhai.gitrelease

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.api.GithubReleaseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class GitRelease(
    private val activity: Activity,
    owner: String,
    repo: String,
    currentVersion: String
) {
    var loading: Boolean = true
    private var releaseDialog: AlertDialog
    private var loadingDialog: AlertDialog
    private var updateDialog: AlertDialog
    var title: String = "Checking new version..."
    var massage: String = "Loading..."
    var releaseTitle = "New version !!"
    var releaseMassage: String? = ""
    private var body: String? = ""
    private var url: String? = null
    private val githubReleaseApi: GithubReleaseApi = GithubReleaseApi(owner, repo, currentVersion)
    private val downloadFilepath: File? = FileUtil(activity).downloadFilePath
    private var apkName: String? = null

    init {
        releaseDialog = AlertDialog.Builder(activity)
            .setView(activity.layoutInflater.inflate(R.layout.dialog_release_new_version, null))
            .setPositiveButton("UPDATE") { _, _ ->
                downloadUpdate()
            }
            .setNegativeButton("Cancel", null)
            .create()
        releaseDialog.setOnShowListener {
            releaseDialog.findViewById<TextView>(R.id.title)!!.text = releaseTitle
            releaseDialog.findViewById<TextView>(R.id.massage)!!.text = releaseMassage
            releaseDialog.findViewById<TextView>(R.id.body)!!.text = body
        }
        loadingDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(massage)
            .setCancelable(false)
            .setNegativeButton("Cancel") { _, _ ->
                cancelCheck()
            }
            .create()
        updateDialog = AlertDialog.Builder(activity)
            .setView(activity.layoutInflater.inflate(R.layout.dialog_progress, null))
            .setCancelable(false)
            .create()
    }

    @SuppressLint("SetTextI18n")
    private fun downloadUpdate() {
        url?.let {
            showUpdate()
            githubReleaseApi.downloadUpdateApk(
                apkName!!,
                it,
                downloadFilepath!!
            ) { percent, current, fileSize, success, file ->
                GlobalScope.launch(Dispatchers.Main) {
                    updateDialog.findViewById<ProgressBar>(R.id.progress)!!.progress = percent
                    updateDialog.findViewById<TextView>(R.id.minMax)!!.text = "$current / $fileSize"
                    updateDialog.findViewById<TextView>(R.id.percent)!!.text = "$percent% "
                }
                if (success) {
                    hideUpdate()
                    Installapk(activity,file)
                }
            }
        }

    }

    fun checkNewVersion() = GlobalScope.launch(Dispatchers.Main) {
        showLoading()
        val data = githubReleaseApi.getReleaseVersion()
        hideLoading()
        if (data.err == null) {
            url = data.downloadUrl
            releaseMassage = data.version + "  [size ${data.size.toString()} mb]"
            body = data.changeLog
            apkName = data.apkName
            try {
                showRelease()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun hideUpdate() {
        updateDialog.dismiss()
    }

    private fun showUpdate() {
        updateDialog.show()
    }

    private fun cancelCheck() = checkNewVersion().cancel()

    private fun hideLoading() {
        if (loading)
            loadingDialog.dismiss()
    }

    private fun showLoading() {
        if (loading)
            loadingDialog.show()
    }

    private fun showRelease() {
        releaseDialog.show()
    }
}