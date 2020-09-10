package com.github.pidsamhai.gitrelease.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

internal fun installApk(context: Context, apkPath: File) {
    try {
        val auth = "${context.packageName}.provider"
        val uri = FileProvider.getUriForFile(context, auth, apkPath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}