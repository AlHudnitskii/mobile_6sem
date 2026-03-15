package com.example.timer.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration

class FontScaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, fontScale: Float): Context {
            val config = Configuration(context.resources.configuration)
            config.fontScale = fontScale
            return FontScaleContextWrapper(context.createConfigurationContext(config))
        }
    }
}
