package com.example.battleship.stats

import androidx.lifecycle.*
import com.example.battleship.common.*
import kotlinx.coroutines.launch

class StatsViewModel(private val repo: SupabaseRepository) : ViewModel() {
    private val _result = MutableLiveData<Result<List<GameRecord>>>()
    val result: LiveData<Result<List<GameRecord>>> = _result
    fun load() = viewModelScope.launch { _result.value = Result.Loading; _result.value = repo.getGameHistory() }
}
