package com.github.pidsamhai.gitrelease

import android.app.NotificationManager
import android.content.Context
import com.github.pidsamhai.gitrelease.worker.GitRelease
import com.google.gson.Gson

internal fun Context.notificationManager(): NotificationManager =
    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

internal fun Context.getReleaseConfig(): GitRelease.Config {
    return Gson().fromJson(
        this.getSharedPreferences(this.getString(R.string.gitrelease_config), Context.MODE_PRIVATE)
            .getString(this.getString(R.string.gitrelease_config_key), null) ?: throw IllegalArgumentException(),
        GitRelease.Config::class.java
    )
}