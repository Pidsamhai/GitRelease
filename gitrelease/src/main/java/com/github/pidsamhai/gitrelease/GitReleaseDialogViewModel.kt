package com.github.pidsamhai.gitrelease

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.pidsamhai.gitrelease.api.GetReleaseResult
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.response.github.GitReleaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class GitReleaseDialogViewModel(val repository: GithubReleaseRepository) : ViewModel() {
    val response = ObservableField<String>("")
    init {
        viewModelScope.launch(Dispatchers.IO) {
            when(val res = repository.getReleaseVersion()) {
                is GetReleaseResult.SuccessNewVersion -> {
                    response.set(res.changeLog)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal class GitReleaseDialogViewModelFactory(val repository: GithubReleaseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GitReleaseDialogViewModel(repository) as T
    }

}