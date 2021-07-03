package com.github.pidsamhai.gitrelease.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.pidsamhai.gitrelease.ui.MarkWon

@BindingAdapter("markdown")
internal fun bindMarkDown(v: TextView, string: String) {
    val mk = MarkWon(v.context).build()
    mk.setMarkdown(v, string)
}