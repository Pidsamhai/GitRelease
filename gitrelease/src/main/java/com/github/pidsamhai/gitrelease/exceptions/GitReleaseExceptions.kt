package com.github.pidsamhai.gitrelease.exceptions

internal class NoReleaseAvailableException : Exception()

internal class NoNewVersionAvailableException : Exception()

internal class CheckVersionException : Exception()

internal class NoFileAssetsException(val changeLog: String? = null) : Exception()

internal class ChecksumException : Exception()