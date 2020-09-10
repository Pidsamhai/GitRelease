package com.github.pidsamhai.gitrelease.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.pidsamhai.gitrelease.receiver.GitReleaseReceiver.ACTIONS.CANCEL
import com.github.pidsamhai.gitrelease.receiver.GitReleaseReceiver.ACTIONS.CHK
import com.github.pidsamhai.gitrelease.receiver.GitReleaseReceiver.ACTIONS.DL
import com.github.pidsamhai.gitrelease.api.NewVersion
import com.github.pidsamhai.gitrelease.worker.GitReleaseDownloadWorker
import com.github.pidsamhai.gitrelease.worker.GitReleaseWorker
import com.google.gson.Gson

internal class GitReleaseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            CHK -> {
                val worker = OneTimeWorkRequestBuilder<GitReleaseWorker>()
                    .build()
                WorkManager.getInstance(context ?: return)
                    .enqueueUniqueWork(
                        GitReleaseWorker.UUID,
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        worker
                    )
            }
            DL -> {
                val newVersion =
                    intent.getParcelableExtra<NewVersion>(GitReleaseDownloadWorker.NEW_VERSION)
                val newVersionJson = Gson().toJson(newVersion)
                val data = Data.Builder()
                    .putString(GitReleaseDownloadWorker.NEW_VERSION, newVersionJson)
                    .build()
                val worker = OneTimeWorkRequestBuilder<GitReleaseDownloadWorker>()
                    .setInputData(data)
                WorkManager.getInstance(context ?: return)
                    .enqueueUniqueWork(
                        GitReleaseDownloadWorker.UUID,
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        worker.build()
                    )
            }
            CANCEL -> {
                WorkManager.getInstance(context ?: return)
                    .cancelUniqueWork(GitReleaseDownloadWorker.UUID)
            }
        }
    }

    object ACTIONS {
        const val DL = "DL"
        const val CHK = "CHK"
        const val CANCEL = "CANCEL"
    }
}