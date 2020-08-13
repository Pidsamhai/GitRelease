package com.github.pidsamhai.gitrelease

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.databinding.DialogReleaseBinding

class GitReleaseDialog(
    val own: String,
    val repo: String,
    val currentVersion: String
) : DialogFragment() {
    private val repository = GithubReleaseRepository(own, repo, currentVersion)
    private var viewModel: GitReleaseDialogViewModel? = null
    private var _dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogReleaseBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, GitReleaseDialogViewModelFactory(repository)).get(
            GitReleaseDialogViewModel::class.java
        )
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.color.bg_popup_transparent)
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        this._dialog?.dismiss()
    }
}