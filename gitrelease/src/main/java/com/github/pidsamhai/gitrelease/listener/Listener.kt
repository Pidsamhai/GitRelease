package com.github.pidsamhai.gitrelease.listener

interface OnCheckReleaseListener {
    fun onComplete()
    fun onCancel()
    fun onCancelDownload()
    fun onCancelUpdate()
}