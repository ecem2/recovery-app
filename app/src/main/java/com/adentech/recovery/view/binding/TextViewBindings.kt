package com.adentech.recovery.view.binding

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.adentech.recovery.extensions.getString
import com.adentech.recovery.extensions.strike
import com.adentech.recovery.extensions.underline
import java.util.*

@BindingAdapter("strike")
fun TextView.bindStrike(isShowStrike: Boolean) {
    strike(isShowStrike)
}

@BindingAdapter("underline")
fun TextView.bindUnderline(isShowUnderline: Boolean) {
    underline(isShowUnderline)
}

@BindingAdapter("message")
fun TextView.bindTextViewMessage(error: Any?) {
    text = error?.getString(context)
}

@BindingAdapter("setTextColor")
fun setTextColor(view: TextView, color: String) {
    val colorOfText = Color.parseColor(color)
    view.setTextColor(colorOfText)
}