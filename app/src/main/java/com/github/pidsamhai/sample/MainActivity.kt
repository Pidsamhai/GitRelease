package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.GitRelease
import com.mukesh.BuildConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val owner = "Pidsamhai"
        val repo = "release_file_test"
        val currentVersion = BuildConfig.VERSION_NAME
        val gitRelease = GitRelease(this, owner, repo, currentVersion).apply {
            loading = true
            title = "Massage Test"
            massage = "Title Test"
        }
        gitRelease.checkNewVersion()
        checkVersion.setOnClickListener {
            gitRelease.checkNewVersion()
        }
    }
}