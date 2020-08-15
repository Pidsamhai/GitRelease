package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.GitRelease
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), GitRelease.OnCheckReleaseListener {

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
        val gitRelease = GitRelease(this, config, this)
        gitRelease.checkUpdate()
        checkVersion.setOnClickListener {
            gitRelease.checkUpdate()
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

    override fun onDownloadError() {
        Log.i(TAG, "onDownloadError: ")
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