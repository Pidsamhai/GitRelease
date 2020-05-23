package com.github.pidsamhai.gitrelease.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.R

internal class UpdateDialog(
    private val activity: Activity,
    private val dialogListener: DialogListener,
    private val darkTheme: Boolean,
    private val progressColor: Int
) {
    private val type = DialogType.Update
    private val builder: AlertDialog.Builder = if (darkTheme)
        AlertDialog.Builder(activity as Context, R.style.AlertDialogDark) else AlertDialog.Builder(
        activity as Context,
        R.style.AlertDialogLight
    )
    private val view = activity.layoutInflater.inflate(R.layout.dialog_update_progress, null)
    private val progress: ProgressBar = view.findViewById(R.id.progress)
    private val minMax: TextView = view.findViewById(R.id.minMax)
    private val percent: TextView = view.findViewById(R.id.percent)
    private val message: TextView = view.findViewById(R.id.massage)
    private val title: TextView = view.findViewById(R.id.title)
    fun build(): AlertDialog {
        builder.apply {
            setView(view)
            title.text = activity.getString(R.string.gitRelease_update)
            message.text = activity.getString(R.string.gitRelease_update_massage)
            setTheme()
            setNegativeButton(R.string.gitRelease_cancel) { _, _ ->
                dialogListener.onNegativeClick(type)
            }
            setPositiveButton(R.string.gitRelease_update) { _, _ ->
                dialogListener.onPositiveClick(type)
            }
            setOnCancelListener {
                dialogListener.onCancelClick(type)
            }
        }
        return builder.create()
    }

    private fun setTheme() {
        Log.e("Dialog", "setTheme: $darkTheme")
        if (darkTheme) {
            title.setTextColor(Color.WHITE)
            message.setTextColor(Color.WHITE)
            minMax.setTextColor(Color.WHITE)
            percent.setTextColor(Color.WHITE)
        }
        progress.progressTintList = ColorStateList.valueOf(progressColor)
    }

    fun getProgressView(): ProgressBar {
        return progress
    }

    fun getMinMaxView(): TextView {
        return minMax
    }

    fun getPercentView(): TextView {
        return percent
    }
}