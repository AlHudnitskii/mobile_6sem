package com.example.timer.ui.main

import androidx.lifecycle.*
import com.example.timer.data.model.Sequence
import com.example.timer.data.repository.SequenceRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repo: SequenceRepository) : ViewModel() {
    val sequences = repo.allSequences
    fun delete(s: Sequence) = viewModelScope.launch { repo.delete(s) }
}

class MainViewModelFactory(private val repo: SequenceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repo) as T
}
