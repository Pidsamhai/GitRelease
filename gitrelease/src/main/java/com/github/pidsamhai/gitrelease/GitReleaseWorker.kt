package com.github.pidsamhai.gitrelease

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.response.Response
import kotlinx.coroutines.coroutineScope
import java.lang.Error

class GitReleaseWorker(
    private val context: Context,
    params: WorkerParameters,
    private val config: GitRelease.Config
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {

        val ID = 10

        val repository = GithubReleaseRepository.getInstance(config)

        val builder = NotificationCompat.Builder(context, "NOTIFICATION_CHANNEL_ID")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_baseline_system_update)
            .setContentText("Anantasak")
            .setContentTitle("CheckUpdate")
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NOTIFICATION_CHANNEL_ID"

            val descriptionText = "NOTIFICATION_CHANNEL_ID"

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val mChannel = NotificationChannel("NOTIFICATION_CHANNEL_ID", name, importance)

            mChannel.description = descriptionText

            notificationManager.createNotificationChannel(mChannel)
        }


        notificationManager.notify(ID, builder.build())

        coroutineScope {
            when (val x = repository.checkNewVersion()) {
                is Response.Success -> {
                    builder.setContentTitle("Found new versions: " + x.value.version)
                    builder.addAction(R.drawable.dialog_background_black,"Update", null)
                    notificationManager.notify(ID, builder.build())
//                    notificationManager.cancel(ID)
                    Result.success()
                }
                is Error -> {
                    builder.setContentTitle(x.message)
                    notificationManager.notify(ID, builder.build())
                    Result.success()
                }
                else -> Result.failure()
            }
        }

    }


    private companion object {
        const val UPDATE_CHANEL_ID = "UPDATE_CHANEL_ID"
    }
}

class GitReleaseWorkerFactory(private val config: GitRelease.Config) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return GitReleaseWorker(appContext, workerParameters, config)
    }
}