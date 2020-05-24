package com.github.pidsamhai.gitrelease.ui

sealed class DialogType {
    object ChangeLog : DialogType()
    object Update : DialogType()
    object Loading : DialogType()
}