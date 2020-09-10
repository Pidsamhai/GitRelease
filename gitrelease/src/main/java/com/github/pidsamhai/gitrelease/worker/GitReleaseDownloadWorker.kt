package com.github.pidsamhai.gitrelease.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.pidsamhai.gitrelease.NotificationUtils
import com.github.pidsamhai.gitrelease.R
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.api.NewVersion
import com.github.pidsamhai.gitrelease.api.OnDownloadListener
import com.github.pidsamhai.gitrelease.getReleaseConfig
import com.github.pidsamhai.gitrelease.notificationManager
import com.github.pidsamhai.gitrelease.receiver.GitReleaseReceiver
import com.github.pidsamhai.gitrelease.util.FileUtil
import com.github.pidsamhai.gitrelease.util.installApk
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import java.io.File

internal class GitReleaseDownloadWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : ListenableWorker(context, workerParameters) {

    private val repository = GithubReleaseRepository.getInstance(context)
    private val dlPath = FileUtil(applicationContext).downloadFilePath
    private val config = context.getReleaseConfig()

    private val cancelIntent = Intent(context, GitReleaseReceiver::class.java).apply {
        action = GitReleaseReceiver.ACTIONS.CANCEL
    }

    private val pendingIntent =
        PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_ONE_SHOT)

    private val builder = NotificationCompat.Builder(context, NotificationUtils.UPDATE_CHANEL_ID)
        .setContentTitle(context.getString(R.string.gitRelease_download))
        .setAutoCancel(false)
        .setSmallIcon(R.drawable.ic_baseline_system_update)
        .setProgress(NotificationUtils.PROGRESS_MIN, NotificationUtils.PROGRESS_MAX, true)
        .addAction(
            R.drawable.ic_baseline_system_update,
            context.getText(R.string.gitRelease_cancel),
            pendingIntent
        )
        .setOngoing(true)
        .setOnlyAlertOnce(true)

    private val notificationManager = context.notificationManager()


    companion object {
        const val UUID = "dl_update_uuid"
        const val NEW_VERSION = "NEW_VERSION"
    }

    override fun onStopped() {
        super.onStopped()
        repository.downloadRequest.cancel()
    }

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { c ->
            val newVersionJson = inputData.getString(NEW_VERSION)
            val newVersion = Gson().fromJson<NewVersion>(newVersionJson, NewVersion::class.java)

            notificationManager.cancel(NotificationUtils.ID)

            notificationManager.apply {
                builder.setProgress(
                    NotificationUtils.PROGRESS_MAX,
                    NotificationUtils.PROGRESS_MIN,
                    true
                )
                notify(NotificationUtils.ID, builder.build())
                repository.downloadFile(
                    newVersion.apkName,
                    newVersion.downloadUrl,
                    dlPath ?: return@getFuture c.set(Result.failure()),
                    object : OnDownloadListener {
                        override fun onError(e: Exception) {
                            notificationManager.cancel(NotificationUtils.ID)
                            c.set(Result.failure())
                        }

                        override fun onSuccess(filePath: File) {
                            notificationManager.cancel(NotificationUtils.ID)
                            if (config.openAfterDownload)
                                installApk(context, filePath)
                            c.set(Result.success())
                        }

                        override fun onProgress(percent: Int, total: Long) {
                            builder
                                .setContentText("$percent%")
                                .setProgress(NotificationUtils.PROGRESS_MAX, percent, false)
                            notify(NotificationUtils.ID, builder.build())
                        }
                    }
                )
            }
        }
    }
}