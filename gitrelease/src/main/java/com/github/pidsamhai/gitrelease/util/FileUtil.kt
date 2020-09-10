package com.github.pidsamhai.gitrelease.util

import android.app.Activity
import android.content.Context
import android.os.Environment

internal class FileUtil(private val context: Context) {
    val downloadFilePath get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}