package com.example.myapplication.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface StringsProvider {
    fun get(@StringRes resId: Int, vararg args: Any): String
}

class DefaultStringsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : StringsProvider {
    override fun get(resId: Int, vararg args: Any): String =
        if (args.isEmpty()) context.getString(resId) else context.getString(resId, *args)
}

