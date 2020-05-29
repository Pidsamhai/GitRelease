package com.github.pidsamhai.gitrelease

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.api.GetReleaseResult
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.api.OnDownloadListener
import com.github.pidsamhai.gitrelease.listener.OnCheckReleaseListener
import com.github.pidsamhai.gitrelease.ui.*
import com.github.pidsamhai.gitrelease.util.FileUtil
import com.github.pidsamhai.gitrelease.util.installApk
import com.github.pidsamhai.gitrelease.util.validateApk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

@SuppressLint("InflateParams")
const val TAG = "GitRelease"

class GitRelease(
        private val activity: Activity,
        owner: String,
        repo: String,
        currentVersion: String
) : CoroutineScope, DialogListener {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    // Setting
    var loading: Boolean = true
    var darkTheme: Boolean = false
    var checksum: Boolean = true
    var progressColor: Int = Color.GREEN

    // Dialog
    private var releaseMassage: String? = ""
    private var releaseDialog: AlertDialog? = null
    private var loadingDialog: AlertDialog? = null
    private var updateDialog: AlertDialog? = null
    private var body: String? = ""

    // Repository
    private val githubReleaseRepository: GithubReleaseRepository =
            GithubReleaseRepository(owner, repo, currentVersion)

    // Download Path
    private val downloadFilepath: File? = FileUtil(
            activity
    ).downloadFilePath

    // Call back Data When Success
    private var updateData: GetReleaseResult.SuccessNewVersion? = null

    // Update Dialog
    private var updateDialogProgress: ProgressBar? = null
    private var updateDialogMinMax: TextView? = null
    private var updateDialogPercent: TextView? = null

    private var checkReleaseJob: Job? = null

    // Global listener
    private var listener: OnCheckReleaseListener? = null


    private fun createReleaseDialog() {
        if (releaseDialog != null) {
            releaseDialog = null
        }
        releaseDialog = ReleaseDialog(activity, this, darkTheme, progressColor).apply {
            setMassage(releaseMassage ?: "Massage")
            setChangLog(body ?: "~~Body~~")
        }.build()
    }

    private fun createUpdateDialog() {
        if (updateDialog == null) {
            updateDialog = UpdateDialog(activity, this, darkTheme, progressColor)
                    .apply {
                        updateDialogProgress = getProgressView()
                        updateDialogMinMax = getMinMaxView()
                        updateDialogPercent = getPercentView()
                    }.build()
        }
    }

    private fun resetUpdateDialogDetail() {
        updateDialogProgress?.isIndeterminate = false
    }


    @SuppressLint("SetTextI18n")
    private fun downloadUpdate() {
        resetUpdateDialogDetail()
        updateData?.let {
            showUpdate()
            githubReleaseRepository.downloadFile(
                    it.apkName,
                    it.downloadUrl,
                    downloadFilepath!!,
                    object : OnDownloadListener {
                        override fun onError(e: Exception) {}
                        override fun onSuccess(filePath: File) {
                            if (checksum)
                                checksum(filePath)
                            else
                                installApk(activity, filePath)
                            hideUpdate()
                        }

                        override fun onProgress(percent: Int, total: Long) {
                            launch(Dispatchers.Main) {
                                updateDialogProgress?.progress = percent
                                updateDialogMinMax?.text = "$percent / $total"
                                updateDialogPercent?.text = "$percent%"
                            }
                        }
                    })
        }
    }

    fun checkNewVersion(onCheckReleaseListener: OnCheckReleaseListener? = null) {
        if (listener == null)
            listener = onCheckReleaseListener
        checkReleaseJob = launch(Dispatchers.Main) {
            showLoading()
            val data = githubReleaseRepository.getReleaseVersion()
            hideLoading()
            when (data) {
                is GetReleaseResult.SuccessNewVersion -> {
                    updateData = data
                    releaseMassage = data.version + "  [size ${data.size} mb]"
                    body = data.changeLog
                    showRelease()
                }
                is GetReleaseResult.Error -> {
                    showErrorMessage(data.error.message ?: "Error")
                    data.error.printStackTrace()
                }
                is GetReleaseResult.SuccessLatestVersion -> {
                    showMessageLatestVersion()
                }
            }
            listener?.onComplete()
        }
    }


    private fun checksum(apk: File) {
        updateDialogProgress?.isIndeterminate = true
        updateData?.let {
        }
        updateData?.let {
            githubReleaseRepository.downloadFile(
                    it.checksumName,
                    it.checksumUrl,
                    downloadFilepath!!,
                    object : OnDownloadListener {
                        override fun onError(e: Exception) {}
                        override fun onSuccess(filePath: File) {
                            if (validateApk(apk, filePath)) {
                                installApk(activity, apk)
                            }
                        }

                        override fun onProgress(percent: Int, total: Long) {}
                    })
        }
    }

    private fun showMessageLatestVersion() {
        Toast.makeText(activity, "You use latest version.", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun hideUpdate() {
        updateDialog?.dismiss()
    }

    private fun showUpdate() {
        createUpdateDialog()
        updateDialog?.show()
    }

    private fun cancelJob() {
        checkReleaseJob?.cancel()
    }

    private fun cancelDownload() {
        githubReleaseRepository.downloadRequest.cancel()
    }

    private fun hideLoading() {
        if (loading)
            loadingDialog?.dismiss()
    }

    private fun showLoading() {
        if (loading)
            createLoadingDialog()
        loadingDialog?.show()
    }

    private fun createLoadingDialog() {
        if (loadingDialog == null)
            loadingDialog = LoadingDialog(activity, this, darkTheme).build()
    }

    private fun showRelease() {
        createReleaseDialog()
        releaseDialog?.show()
    }

    override fun onNegativeClick(dialogType: DialogType) {
        when (dialogType) {
            is DialogType.Update -> {
                cancelDownload()
                listener?.onCancelDownload()
            }
            is DialogType.Loading -> {
                cancelJob()
                listener?.onCancel()
            }
            is DialogType.ChangeLog -> {
                listener?.onCancelUpdate()
            }
        }
    }

    override fun onPositiveClick(dialogType: DialogType) {
        when (dialogType) {
            is DialogType.ChangeLog -> {
                downloadUpdate()
            }
        }
    }

    override fun onCancelClick(dialogType: DialogType) {}
}
