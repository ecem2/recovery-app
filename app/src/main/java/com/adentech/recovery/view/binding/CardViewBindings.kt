package com.adentech.recovery.view.binding

import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter

@BindingAdapter("setCardBackgroundColor")
fun setCardBackgroundColor(view: CardView, color: Int) {
    view.setCardBackgroundColor(color)
}