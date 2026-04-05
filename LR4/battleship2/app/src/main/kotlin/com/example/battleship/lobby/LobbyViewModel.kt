package com.example.battleship.lobby

import androidx.lifecycle.*
import com.example.battleship.common.*
import kotlinx.coroutines.launch

class LobbyViewModel(private val repo: SupabaseRepository) : ViewModel() {

    private val _nickname     = MutableLiveData("Player")
    val nickname: LiveData<String> = _nickname

    private val _createResult = MutableLiveData<Result<String>>()
    val createResult: LiveData<Result<String>> = _createResult

    private val _joinResult   = MutableLiveData<Result<GameRow>>()
    val joinResult: LiveData<Result<GameRow>> = _joinResult

    init {
        loadNickname()
    }

    fun loadNickname() {
        viewModelScope.launch {
            when (val r = repo.getProfile()) {
                is Result.Success -> _nickname.value = r.data.nickname.ifBlank { "Player" }
                else -> {}
            }
        }
    }

    fun createGame() {
        viewModelScope.launch {
            _createResult.value = Result.Loading
            val nick = _nickname.value ?: "Player"
            _createResult.value = repo.createGame(nick)
        }
    }

    fun joinGame(gameId: String) {
        viewModelScope.launch {
            _joinResult.value = Result.Loading
            val nick = _nickname.value ?: "Player"
            _joinResult.value = repo.joinGame(gameId.trim().uppercase(), nick)
        }
    }
}
