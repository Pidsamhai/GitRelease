package com.github.pidsamhai.gitrelease.ui

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.api.NewVersion
import com.github.pidsamhai.gitrelease.exceptions.NoFileAssetsException
import com.github.pidsamhai.gitrelease.exceptions.NoNewVersionAvailableException
import com.github.pidsamhai.gitrelease.exceptions.NoReleaseAvailableException
import com.github.pidsamhai.gitrelease.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class GitReleaseDialogViewModel(private val repository: GithubReleaseRepository) :
    ViewModel() {
    var newVersion: NewVersion? = null
    private var checkUpdateJob: Job? = null
    var listener: GitRelease.OnCheckReleaseListener? = null
    private val _closeDialog = MutableLiveData<Boolean>(false)
    val closeDialog = _closeDialog
    val changeLog = ObservableField<String>("")
    val isShowChangeLog = ObservableBoolean(false)
    val description = ObservableField<String>("")
    val canUpdate = ObservableBoolean(true)

    fun checkUpdate() {
        checkUpdateJob = viewModelScope.launch(Dispatchers.IO) {
            when (val res = repository.checkNewVersion()) {
                is Response.Error -> {
                    when (res.err) {
                        is NoFileAssetsException -> {
                            changeLog.set(res.err.changeLog)
                            isShowChangeLog.set(true)
                            canUpdate.set(false)
                            description.set("No file Asset")
                        }
                        is NoReleaseAvailableException -> {
                            _closeDialog.postValue(true)
                            listener?.onCompleteNoUpdateFound()
                        }
                        is NoNewVersionAvailableException -> {
                            _closeDialog.postValue(true)
                            listener?.onCompleteLatestVersion()
                        }
                        else -> {
                            _closeDialog.postValue(true)
                            listener?.onError()
                        }
                    }
                }
                is Response.Success -> {
                    newVersion = res.value
                    changeLog.set(res.value.changeLog)
                    val size = newVersion?.size?.div(1024f)
                    description.set(String.format("v%s [ size %.2fmb ]",newVersion?.version, size ?: 0f))
                    isShowChangeLog.set(true)
                }
            }
        }
    }

    fun cancelCheckUpdate() {
        checkUpdateJob?.cancel()
    }
}

@Suppress("UNCHECKED_CAST")
internal class GitReleaseDialogViewModelFactory(private val repository: GithubReleaseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GitReleaseDialogViewModel(
            repository
        ) as T
    }
}