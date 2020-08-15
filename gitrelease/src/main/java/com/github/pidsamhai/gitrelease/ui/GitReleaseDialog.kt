package com.github.pidsamhai.gitrelease.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.R
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.databinding.DialogReleaseBinding

internal class GitReleaseDialog(
    config: GitRelease.Config,
    private val listener: GitRelease.OnCheckReleaseListener
) : DialogFragment() {

    private val repository = GithubReleaseRepository(config)
    private var viewModel: GitReleaseDialogViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogReleaseBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            this,
            GitReleaseDialogViewModelFactory(
                repository
            )
        ).get(
            GitReleaseDialogViewModel::class.java
        )
        viewModel?.closeDialog?.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.dismiss()
            }
        })
        viewModel?.listener = listener
        binding.vm = viewModel
        binding.btnCancelCheck.setOnClickListener {
            viewModel?.cancelCheckUpdate()
            listener.onCancelCheckUpdate()
            this.dismiss()
        }
        binding.btnCancelUpdate.setOnClickListener {
            listener.onUpdateCancel()
            this.dismiss()
        }
        binding.btnOK.setOnClickListener {
            val dlDialog =
                DownloadDialog(
                    repository,
                    viewModel?.newVersion ?: return@setOnClickListener,
                    listener
                )
            dlDialog.isCancelable = false
            dlDialog.show(requireFragmentManager(), requireActivity().javaClass.simpleName)
            this.dismiss()
        }
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
        viewModel?.checkUpdate()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }
}