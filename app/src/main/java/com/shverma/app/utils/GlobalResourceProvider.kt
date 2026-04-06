package com.shverma.app.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes

@SuppressLint("StaticFieldLeak")
object GlobalResourceProvider {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getGlobalString(@StringRes resId: Int): String = context.getString(resId)
}