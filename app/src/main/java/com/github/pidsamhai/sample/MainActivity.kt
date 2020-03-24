package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.pidsamhai.gitrelease.GitRelease
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gitRelease = GitRelease(this).apply {
//            owner = "anantasak-pcru"
//            repo = "Test-release"
            loading = false
            owner = "Pidsamhai"
            repo = "android_studio_class_exam"
            currentVersion = BuildConfig.VERSION_NAME
            title = "Massage Test"
            massage = "Title Test"
        }
        gitRelease.getRelease()
        checkVersion.setOnClickListener {
            gitRelease.getRelease()
        }
    }
}