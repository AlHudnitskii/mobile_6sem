package com.example.battleship.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.battleship.common.SupabaseRepository

class RepoViewModelFactory(private val repo: SupabaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor(SupabaseRepository::class.java)
        return constructor.newInstance(repo)
    }
}
