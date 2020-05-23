package com.github.pidsamhai.gitrelease.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.R

interface DialogListener {
    fun onNegativeClick(dialogType: DialogType)
    fun onPositiveClick(dialogType: DialogType)
    fun onCancelClick(dialogType: DialogType)
}


internal class ChangeLogDialog(
    private val activity: Activity,
    private val dialogListener: DialogListener,
    private val darkTheme: Boolean = true,
    private val progressColor: Int
) {
    private val type = DialogType.ChangeLog
    private val builder: AlertDialog.Builder = if (darkTheme)
        AlertDialog.Builder(activity as Context, R.style.AlertDialogDark) else AlertDialog.Builder(
        activity as Context,
        R.style.AlertDialogLight
    )
    private val view = activity.layoutInflater.inflate(R.layout.dialog_release_new_version, null)
    private val title = view.findViewById<TextView>(R.id.title)
    private val massage = view.findViewById<TextView>(R.id.massage)
    private val markWon = MarkWon(activity as Context, darkTheme, progressColor).build()
    private val mkView = view.findViewById<TextView>(R.id.t_mk_render)
    fun build(): AlertDialog {
        title.text = activity.getString(R.string.gitRelease_new_version)
        builder.apply {
            setView(view)
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
        if (darkTheme) {
            title.setTextColor(Color.WHITE)
            massage.setTextColor(Color.WHITE)
            mkView.setTextColor(Color.WHITE)
        }
    }

    fun setChangLog(body: String) {
        markWon.setMarkdown(mkView, body)
    }

    fun setMassage(msg: String) {
        this.massage.text = msg
    }
}