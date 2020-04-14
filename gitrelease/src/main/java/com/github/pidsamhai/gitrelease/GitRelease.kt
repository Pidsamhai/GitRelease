package com.github.pidsamhai.gitrelease

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.util.FileUtil
import com.github.pidsamhai.gitrelease.util.installApk
import com.github.pidsamhai.gitrelease.util.validateApk
import com.mukesh.MarkdownView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

@SuppressLint("InflateParams")
class GitRelease(
    private val activity: Activity,
    owner: String,
    repo: String,
    currentVersion: String
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    var loading: Boolean = true
    private var releaseDialog: AlertDialog
    private var loadingDialog: AlertDialog
    private var updateDialog: AlertDialog
    var title: String = "Checking new version..."
    var massage: String = "Loading..."
    var releaseTitle = "New version !!"
    var releaseMassage: String? = ""
    var checksum: Boolean = true
    private var body: String? = ""
    private val githubReleaseRepository: GithubReleaseRepository =
        GithubReleaseRepository(owner, repo, currentVersion)
    private val downloadFilepath: File? = FileUtil(
        activity
    ).downloadFilePath
    private var updateData: UpdateData? = null
    private val releaseView: View =
        activity.layoutInflater.inflate(R.layout.dialog_release_new_version, null)
    private val updateView: View =
        activity.layoutInflater.inflate(R.layout.dialog_update_progress, null)
    private val updateDialogProgress: ProgressBar = updateView.findViewById(R.id.progress)
    private val updateDialogMinMax: TextView = updateView.findViewById(R.id.minMax)
    private val updateDialogPercent: TextView = updateView.findViewById(R.id.percent)
    private val updateDialogMassage: TextView = updateView.findViewById(R.id.massage)
    private val updateDialogTitle: TextView = updateView.findViewById(R.id.title)
    private val releaseDialogTitle: TextView = releaseView.findViewById(R.id.title)
    private val releaseDialogMassage: TextView = releaseView.findViewById(R.id.massage)
    private val releaseDialogMarkdownView: MarkdownView = releaseView.findViewById(R.id.mkView)

    init {
        releaseDialog = AlertDialog.Builder(activity)
            .setView(releaseView)
            .setPositiveButton(R.string.gitRelease_update) { _, _ ->
                downloadUpdate()
            }
            .setNegativeButton(R.string.gitRelease_cancel, null)
            .create()
        releaseDialog.setOnShowListener {
            releaseDialogTitle.text = releaseTitle
            releaseDialogMassage.text = releaseMassage
            releaseDialogMarkdownView.setMarkDownText(body)
        }
        loadingDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(massage)
            .setCancelable(false)
            .setNegativeButton(R.string.gitRelease_cancel) { _, _ ->
                cancelJob()
            }
            .create()
        updateDialog = AlertDialog.Builder(activity)
            .setView(updateView)
            .setCancelable(false)
            .setNegativeButton(R.string.gitRelease_cancel) { _, _ ->
                cancelDownload()
            }
            .create()
    }

    private fun resetUpdateDialogDetail() {
        updateDialogProgress.isIndeterminate = false
        activity.resources.also {
            updateDialogTitle.text = it.getString(R.string.gitRelease_update)
            updateDialogMassage.text = it.getString(R.string.gitRelease_update_massage)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun downloadUpdate() {
        resetUpdateDialogDetail()
        updateData?.let {
            showUpdate()
            githubReleaseRepository.downloadFile(
                it.apkName!!,
                it.downloadUrl!!,
                downloadFilepath!!
            ) { percent, current, fileSize, success, file ->
                launch(Dispatchers.Main) {
                    updateDialogProgress.progress = percent
                    updateDialogMinMax.text = "$current / $fileSize"
                    updateDialogPercent.text = "$percent% "
                }
                if (success) {
                    if (checksum)
                        checksum(file)
                    else
                        installApk(activity, file)
                    hideUpdate()
                }
            }
        }
    }

    fun checkNewVersion() {
        launch(Dispatchers.Main) {
            showLoading()
            val data = githubReleaseRepository.getReleaseVersion()
            hideLoading()
            if (data.err == null && data.newVersion) {
                updateData = data
                releaseMassage = data.version + "  [size ${data.size.toString()} mb]"
                body = data.changeLog
                try {
                    showRelease()
                } catch (e: Exception) {
                    showMessageLatestVersion()
                    e.printStackTrace()
                }
            } else {
                showMessageLatestVersion()
            }
        }
    }

    private fun checksum(apk: File) {
        updateDialogProgress.isIndeterminate = true
        updateDialogMassage.text = activity.resources.getString(R.string.gitRelease_checkMassage)
        updateDialogTitle.text = activity.resources.getString(R.string.gitRelease_checkTitle)
        updateData?.let {
            githubReleaseRepository.downloadFile(
                it.checksumName!!,
                it.checksumUrl!!,
                downloadFilepath!!
            ) { _, _, _, success, file ->
                if (success) {
                    hideUpdate()
                    val v = validateApk(
                        apk,
                        file
                    )
                    Log.e("checksum", "$v")
                    if (v)
                        installApk(
                            activity,
                            apk
                        )
                }
            }
        }

    }

    private fun showMessageLatestVersion() {
        Toast.makeText(activity, "You use latest version.", Toast.LENGTH_SHORT).show()
    }

    private fun hideUpdate() {
        updateDialog.dismiss()
    }

    private fun showUpdate() {
        updateDialog.show()
    }

    private fun cancelJob() {
        job.cancel()
    }

    private fun cancelDownload() {
        githubReleaseRepository.downloadRequest.cancel()
    }

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
