package com.github.pidsamhai.gitrelease

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

fun Installapk(activity: Activity, apkPath: File) {
    try {
        val auth = "com.github.pidsamhai.sample"
        val uri = FileProvider.getUriForFile(activity, auth, apkPath)
        Log.e(ContentValues.TAG, "Path: $apkPath")
        Log.e(ContentValues.TAG, "File Path: $apkPath")
        Log.e(ContentValues.TAG, "Provider Apk Path: $uri")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}