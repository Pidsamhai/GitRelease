package com.github.pidsamhai.gitrelease.ui

import android.content.Context
import android.graphics.Color
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

internal class MarkWon(
    context: Context,
    progressColor: Int? = null
) {
    private val builder = Markwon.builder(context)
    init {
        builder
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.linkColor(progressColor ?: Color.BLUE)
                }
            })
    }

    fun build() = builder.build()
}