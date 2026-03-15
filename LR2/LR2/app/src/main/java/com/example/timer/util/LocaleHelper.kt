package com.example.timer.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREF = "settings"
    private const val KEY_LANG = "language"

    fun setLocale(context: Context): Context {
        val lang = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "ru") ?: "ru"
        return updateResources(context, lang)
    }

    fun setNewLocale(context: Context, lang: String): Context {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
        return updateResources(context, lang)
    }

    private fun updateResources(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
