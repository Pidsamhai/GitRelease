package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.GitRelease
import kotlinx.android.synthetic.main.activity_main.*

val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), GitRelease.OnCheckReleaseListener {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val owner = "Pidsamhai" // Owner Name
        val repo = "release_file_test" // Repository name
        val currentVersion = BuildConfig.VERSION_NAME
        val gitRelease = GitRelease(this, owner, repo, currentVersion).apply {
            loading = true
            checksum = true
            darkTheme = false
            progressColor = Color.YELLOW
        }
        gitRelease.checkNewVersion(this)
        checkVersion.setOnClickListener {
            gitRelease.checkNewVersion()
        }
    }

    override fun onComplete() {
        Log.i(TAG, "onComplete: ")
    }

    override fun onCancel() {
        Log.i(TAG, "onCancel: ")
    }

    override fun onCancelDownload() {
        Log.i(TAG, "onCancelDownload: ")
    }

    override fun onCancelUpdate() {
        Log.i(TAG, "onCancelUpdate: ")
    }
}