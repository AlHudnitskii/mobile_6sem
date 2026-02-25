package com.example.converter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), KeyboardFragment.OnKeyPressListener {

    private lateinit var dataFragment: DataFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            dataFragment = DataFragment()
            val keyboardFragment = KeyboardFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentData, dataFragment)
                .replace(R.id.fragmentKeyboard, keyboardFragment)
                .commit()
        } else {
            dataFragment = supportFragmentManager.findFragmentById(R.id.fragmentData) as DataFragment
        }
    }

    override fun onKeyPressed(key: String) {
        dataFragment.onKeyPressed(key)
    }
}
