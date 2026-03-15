package com.example.timer

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.timer.databinding.ActivityMainBinding
import com.example.timer.util.FontScaleContextWrapper
import com.example.timer.util.LocaleHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        val localeContext = LocaleHelper.setLocale(newBase)
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val scale = prefs.getFloat("font_scale", 1.0f)
        super.attachBaseContext(FontScaleContextWrapper.wrap(localeContext, scale))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = prefManager.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val appBarConfig = AppBarConfiguration(setOf(R.id.mainFragment))
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
