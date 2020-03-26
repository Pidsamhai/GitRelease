package com.github.pidsamhai.gitrelease

import android.app.Activity
import android.os.Environment

class FileUtil(private val activity: Activity) {
    val downloadFilePath get() = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}