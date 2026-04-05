package com.example.battleship

import android.app.Application
import com.example.battleship.common.SupabaseRepository

class App : Application() {
    // Single repository instance shared across ViewModels via ViewModelFactory
    val repository: SupabaseRepository by lazy { SupabaseRepository(this) }
}
