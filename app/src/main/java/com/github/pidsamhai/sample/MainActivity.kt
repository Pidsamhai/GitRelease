package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.worker.GitRelease
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), GitRelease.OnCheckReleaseListener {


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = GitRelease.Config(
            owner = "Pidsamhai" /* Owner Name */,
            repo = "release_file_test" /* Repository name */,
            currentVersion = BuildConfig.VERSION_NAME,
            checksum = true,
            openAfterDownload = true
        )

        GitRelease.initConfig(config, this) // Init config

        GitRelease.startWorker(this) // start background service check update notification

        checkVersion.setOnClickListener {
            GlobalScope.launch {
                GitRelease(this@MainActivity).checkUpdateNoLoading(this@MainActivity)
            }
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