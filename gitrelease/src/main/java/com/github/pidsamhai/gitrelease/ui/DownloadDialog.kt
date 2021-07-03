package com.github.pidsamhai.gitrelease.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.pidsamhai.gitrelease.GitRelease
import com.github.pidsamhai.gitrelease.api.GithubReleaseRepository
import com.github.pidsamhai.gitrelease.api.NewVersion
import com.github.pidsamhai.gitrelease.databinding.DialogUpdateProgressBinding
import com.github.pidsamhai.gitrelease.util.FileUtil
import java.io.File

internal class DownloadDialog(
    private val repository: GithubReleaseRepository,
    private val newVersion: NewVersion,
    private val listener: GitRelease.OnCheckReleaseListener?
) : DialogFragment() {

    private var viewModel: DownloadDialogViewModel? = null
    private var downloadFilePath: File? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogUpdateProgressBinding.inflate(inflater, container, false)
        downloadFilePath = FileUtil(requireActivity()).downloadFilePath
        viewModel = ViewModelProvider(
            this,
            DownloadDialogViewModelFactory(
                repository, newVersion, requireActivity().application
            )
        ).get(DownloadDialogViewModel::class.java)
        viewModel?.listener = listener
        viewModel?.closeDialog?.observe(viewLifecycleOwner, Observer {
            this.dismiss()
        })
        binding.vm = viewModel
        binding.btnCancel.setOnClickListener {
            viewModel?.cancelDownload()
            this.dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel?.downLoad(downloadFilePath ?: return)
    }
}