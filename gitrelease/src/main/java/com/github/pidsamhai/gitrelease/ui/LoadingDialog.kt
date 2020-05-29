package com.github.pidsamhai.gitrelease.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.pidsamhai.gitrelease.R

internal class LoadingDialog(
    private val activity: Activity,
    private val dialogListener: DialogListener,
    private val darkTheme: Boolean = true
) {
    private val type = DialogType.Loading
    private val view = activity.layoutInflater.inflate(R.layout.dialog_release_new_version, null)
    private val title = view.findViewById<TextView>(R.id.title)
    private val mkView = view.findViewById<TextView>(R.id.t_mk_render)
    private val message = view.findViewById<TextView>(R.id.massage)
    private val builder: AlertDialog.Builder = if (darkTheme)
        AlertDialog.Builder(activity as Context, R.style.AlertDialogDark) else AlertDialog.Builder(
        activity as Context,
        R.style.AlertDialogLight
    )

    fun build(): AlertDialog {
        title.text = activity.getString(R.string.gitRelease_checkTitle)
        message.text = activity.getString(R.string.gitRelease_checkMassage)
        mkView.visibility = View.GONE
        builder.apply {
            setView(view)
            setTheme()
            setNegativeButton(R.string.gitRelease_cancel) { _, _ ->
                dialogListener.onNegativeClick(type)
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
            message.setTextColor(Color.WHITE)
        }
    }
}