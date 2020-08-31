package com.github.pidsamhai.sample

import android.app.Application
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.GitReleaseWorker
import com.github.pidsamhai.gitrelease.GitReleaseWorkerFactory

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        val owner = "Pidsamhai" // Owner Name
        val repo = "release_file_test" // Repository name
        val currentVersion = BuildConfig.VERSION_NAME
        val config = GitRelease.Config(
            owner,
            repo,
            currentVersion,
            true
        )

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<GitReleaseWorker>().build()
        val configx = Configuration.Builder()
            .setWorkerFactory(GitReleaseWorkerFactory(config))
            .build()

        WorkManager.initialize(this, configx)

        val wmk = WorkManager.getInstance(this)

        wmk.enqueue(workRequest)
    }
}