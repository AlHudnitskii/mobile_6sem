package com.example.timer.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.example.timer.R
import com.example.timer.data.db.AppDatabase
import com.example.timer.util.LocaleHelper
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("dark_theme")?.setOnPreferenceChangeListener { _, newValue ->
            val mode = if (newValue as Boolean) AppCompatDelegate.MODE_NIGHT_YES
                       else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            true
        }

        findPreference<SeekBarPreference>("font_scale")?.setOnPreferenceChangeListener { _, newValue ->
            val scale = (newValue as Int) / 100f
            requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putFloat("font_scale", scale).apply()
            requireActivity().recreate()
            true
        }

        findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
            LocaleHelper.setNewLocale(requireContext(), newValue.toString())
            requireActivity().recreate()
            true
        }

        findPreference<Preference>("clear_data")?.setOnPreferenceClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(requireContext()).sequenceDao().deleteAll()
            }
            true
        }
    }
}
