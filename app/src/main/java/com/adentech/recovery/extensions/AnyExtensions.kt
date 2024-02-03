package com.adentech.recovery.extensions

import android.content.Context
import com.adentech.recovery.common.Constants.EMPTY_STRING

fun Any.getString(context: Context): String {
    return when (this) {
        is String -> {
            this
        }
        is Int -> {
            context.getString(this)
        }
        else -> {
            EMPTY_STRING
        }
    }
}