package com.github.pidsamhai.sample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.response.ReleaseResponse
import kotlinx.coroutines.launch

class VModel : ViewModel() {
    val release: MutableLiveData<ReleaseResponse> = MutableLiveData()
    private lateinit var gitRelease: GitRelease

    fun getRelease() = viewModelScope.launch {
        gitRelease = GitRelease().apply {
            owner = "anantasak-pcru"
//            owner = "Pidsamhai"
//            repo = "android_studio_class_exam"
            repo = "Test-release"
            currentVersion = BuildConfig.VERSION_NAME
        }
        val data = gitRelease.getRelease()
        release.postValue(data)
    }
}