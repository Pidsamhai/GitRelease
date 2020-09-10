package com.github.pidsamhai.gitrelease.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.pidsamhai.gitrelease.NotificationUtils
import com.github.pidsamhai.gitrelease.R
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.getReleaseConfig
import com.github.pidsamhai.gitrelease.receiver.GitReleaseReceiver
import com.github.pidsamhai.gitrelease.response.Response
import kotlinx.coroutines.coroutineScope
import java.lang.Error

internal class GitReleaseWorker(
    private val context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val repository = GithubReleaseRepository.getInstance(context)

        val builder = NotificationCompat.Builder(context, NotificationUtils.UPDATE_CHANEL_ID)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_baseline_system_update)
            .setContentText(context.getText(R.string.gitRelease_new_update))
            .setContentTitle("CheckUpdate")
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setSound(null)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Check update"

            val descriptionText = "Check for new version"

            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(NotificationUtils.UPDATE_CHANEL_ID, name, importance)
                .apply {
                    description = descriptionText
                }

            notificationManager.createNotificationChannel(channel)
        }


        notificationManager.notify(NotificationUtils.ID, builder.build())

        coroutineScope {
            when (val x = repository.checkNewVersion()) {
                is Response.Success -> {

                    val dlIntent = Intent(context, GitReleaseReceiver::class.java).apply {
                        action = GitReleaseReceiver.ACTIONS.DL
                        putExtra(GitReleaseDownloadWorker.NEW_VERSION, x.value)
                    }

                    val dlPendingIntent = PendingIntent.getBroadcast(context, 0, dlIntent, PendingIntent.FLAG_ONE_SHOT)

                    builder.setContentText("v " + x.value.version + "   size [ %.2fmb ]".format(x.value.size / 1024.0))
                    builder.setContentTitle(context.getText(R.string.gitRelease_new_update))
                    builder.addAction(R.drawable.dialog_background_black, "Update", dlPendingIntent)
                    notificationManager.notify(NotificationUtils.ID, builder.build())
                    Result.success()
                }
                is Error -> {
                    builder.setContentTitle(x.message)
                    notificationManager.notify(NotificationUtils.ID, builder.build())
                    Result.success()
                }
                else -> Result.failure()
            }
        }

    }


    companion object {
        const val UUID = "check_update_uuid"
    }
}