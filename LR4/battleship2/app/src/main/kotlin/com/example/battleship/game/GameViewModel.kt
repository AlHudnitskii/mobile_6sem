package com.example.battleship.game

import androidx.lifecycle.*
import com.example.battleship.common.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GameViewModel(private val repo: SupabaseRepository) : ViewModel() {

    private lateinit var gameId: String
    private var isPlayer1 = true
    private var latestGame: GameRow? = null
    private var observerJob: Job? = null

    private val _myBoard      = MutableLiveData<List<Int>>()
    val myBoard: LiveData<List<Int>> = _myBoard
    private val _enemyBoard   = MutableLiveData<List<Int>>()
    val enemyBoard: LiveData<List<Int>> = _enemyBoard
    private val _isMyTurn     = MutableLiveData(false)
    val isMyTurn: LiveData<Boolean> = _isMyTurn
    private val _statusText   = MutableLiveData<String>()
    val statusText: LiveData<String> = _statusText
    private val _waiting      = MutableLiveData(true)
    val waiting: LiveData<Boolean> = _waiting
    private val _gameFinished = MutableLiveData<Pair<Boolean?, String>>()
    val gameFinished: LiveData<Pair<Boolean?, String>> = _gameFinished
    private val _error        = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun init(gameId: String, isPlayer1: Boolean) {
        if (this::gameId.isInitialized) return
        this.gameId    = gameId
        this.isPlayer1 = isPlayer1
        startObserving()
    }

    private fun startObserving() {
        observerJob = viewModelScope.launch {
            repo.observeGame(gameId)
                .catch { e -> _error.value = e.localizedMessage }
                .collect { game -> game?.let { latestGame = it; applyState(it) } }
        }
    }

    private fun applyState(g: GameRow) {
        val status = g.gameStatus()
        _myBoard.value    = if (isPlayer1) g.player1Board else g.player2Board
        _enemyBoard.value = if (isPlayer1) g.player2Board else g.player1Board

        _isMyTurn.value = when (status) {
            GameStatus.PLAYER1_TURN -> isPlayer1
            GameStatus.PLAYER2_TURN -> !isPlayer1
            else -> false
        }
        _waiting.value  = status == GameStatus.WAITING_FOR_PLAYER || status == GameStatus.PLACEMENT

        _statusText.value = when (status) {
            GameStatus.WAITING_FOR_PLAYER -> "Waiting for opponent to join…"
            GameStatus.PLACEMENT          -> "Both players setting up ships…"
            GameStatus.PLAYER1_TURN -> if (isPlayer1) "Your turn — tap to fire!" else "${g.player1Nickname}'s turn…"
            GameStatus.PLAYER2_TURN -> if (!isPlayer1) "Your turn — tap to fire!" else "${g.player2Nickname}'s turn…"
            GameStatus.FINISHED     -> if (g.winnerId == repo.currentUserId) "🎉 You won!" else "💀 You lost!"
        }

        if (status == GameStatus.FINISHED) {
            _isMyTurn.value = false
            _gameFinished.value = Pair(g.winnerId == repo.currentUserId, g.winnerId ?: "")
        }
    }

    fun fireShot(row: Int, col: Int) {
        val game = latestGame ?: return
        if (_isMyTurn.value != true) return
        viewModelScope.launch {
            val r = repo.fireShot(gameId, row, col, isPlayer1, game)
            if (r is Result.Error) _error.value = r.message
        }
    }

    fun clearError() { _error.value = null }
    override fun onCleared() { super.onCleared(); observerJob?.cancel() }
}
