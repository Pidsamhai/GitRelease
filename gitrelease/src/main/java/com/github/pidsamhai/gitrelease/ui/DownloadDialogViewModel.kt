package com.github.pidsamhai.gitrelease.ui

import android.app.Application
import android.util.Log
import androidx.databinding.*
import androidx.lifecycle.*
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.R
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.api.NewVersion
import com.github.pidsamhai.gitrelease.api.OnDownloadListener
import com.github.pidsamhai.gitrelease.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

internal class DownloadDialogViewModel(
    private val repository: GithubReleaseRepository,
    private val newVersion: NewVersion,
    app: Application
) :
    AndroidViewModel(app), OnDownloadListener {
    private val context = app.applicationContext
    val progress = ObservableInt(0)
    val dlProgress = ObservableFloat(0f)
    val dlSize = newVersion.size / 1024f
    val isChecksum = ObservableBoolean(false)
    val progressMsg = ObservableField<String>(context.getString(R.string.gitRelease_update_massage))
    var listener: GitRelease.OnCheckReleaseListener? = null
    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog = _closeDialog

    fun downLoad(
        downloadFilePath: File
    ) {
        Log.i("TAG", "downLoad: ")
        repository.downloadFile(
            newVersion.apkName,
            newVersion.downloadUrl,
            downloadFilePath,
            this@DownloadDialogViewModel
        )
    }

    override fun onError(e: Exception) {
        listener?.onDownloadError()
        _closeDialog.postValue(true)
    }

    override fun onSuccess(filePath: File) {
        if (repository.config.checksum) {
            isChecksum.set(true)
            progressMsg.set("CheckSum apk")
            checkSum(filePath)
        } else {
            listener?.onDownloadComplete(filePath)
            _closeDialog.postValue(true)
        }
    }

    override fun onProgress(percent: Int, total: Long) {
        progress.set(percent)
        val dlS = ((percent * total / 100f) / 1024f) / 1024f
        Log.i("TAG", "onProgress: $dlS")
        dlProgress.set(dlS)
    }

    private fun checkSum(apk: File) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val res = repository.checksum(newVersion.checksumUrl, apk)) {
                is Response.Error -> {
                    listener?.onChecksumError()
                }
                is Response.Success -> {
                    if (res.value) {
                        listener?.onDownloadComplete(apk)
                    } else {
                        listener?.onChecksumError()
                    }
                }
            }
            isChecksum.set(false)
            _closeDialog.postValue(true)
        }
    }

    fun cancelDownload() {
        repository.downloadRequest.cancel()
        listener?.onDownloadCancel()
    }
}

@Suppress("UNCHECKED_CAST")
internal class DownloadDialogViewModelFactory(
    private val repository: GithubReleaseRepository,
    private val newVersion: NewVersion,
    private val app: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DownloadDialogViewModel(
            repository,
            newVersion,
            app
        ) as T
    }
}