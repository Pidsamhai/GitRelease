package com.github.pidsamhai.gitrelease.util

import android.app.Activity
import android.os.Environment

internal class FileUtil(private val activity: Activity) {
    val downloadFilePath get() = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}