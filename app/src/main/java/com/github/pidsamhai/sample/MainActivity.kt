package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.os.ConfigurationCompat
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.GitReleaseWorker
import com.github.pidsamhai.gitrelease.GitReleaseWorkerFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), GitRelease.OnCheckReleaseListener {

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val owner = "Pidsamhai" // Owner Name
        val repo = "release_file_test" // Repository name
        val currentVersion = BuildConfig.VERSION_NAME
        val config = GitRelease.Config(
            owner,
            repo,
            currentVersion,
            true
        )
        val gitRelease = GitRelease(this, config)
//        gitRelease.checkUpdate(this)
        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<GitReleaseWorker>().build()
//        val configx = Configuration.Builder()
//            .setWorkerFactory(GitReleaseWorkerFactory(config))
//            .build()
//
//        WorkManager.initialize(this, configx)
//        val wmk = WorkManager.getInstance(this)
        checkVersion.setOnClickListener {

           WorkManager.getInstance(this)
               .enqueue(workRequest)

        }
    }

    override fun onCompleteNoUpdateFound() {
        Log.i(TAG, "onCompleteNoUpdateFound: ")
    }

    override fun onCancelCheckUpdate() {
        Log.i(TAG, "onCancelCheckUpdate: ")
    }

    override fun onError() {
        Log.i(TAG, "onError: ")
    }

    override fun onDownloadCancel() {
        Log.i(TAG, "onDownloadCancel: ")
    }

    override fun onUpdateCancel() {
        Log.i(TAG, "onUpdateCancel: ")
    }

    override fun onDownloadComplete(apk: File) {
        GitRelease.installApk(this, apk)
    }

    override fun onChecksumError() {
        Log.i(TAG, "onChecksumError: ")
    }

    override fun onCompleteLatestVersion() {
        Log.i(TAG, "onCompleteLatestVersion: ")
    }
}