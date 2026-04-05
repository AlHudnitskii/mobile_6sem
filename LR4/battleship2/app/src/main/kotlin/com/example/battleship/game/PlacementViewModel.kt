package com.example.battleship.game

import androidx.lifecycle.*
import com.example.battleship.common.*
import kotlinx.coroutines.launch

class PlacementViewModel(private val repo: SupabaseRepository) : ViewModel() {

    private lateinit var gameId: String
    private var isPlayer1 = true

    // Placed ships and remaining sizes to place
    private val placed = mutableListOf<Ship>()
    private val remainingQueue = mutableListOf<Int>()

    private val _board     = MutableLiveData<List<Int>>(List(100) { 0 })
    val board: LiveData<List<Int>> = _board

    private val _dir       = MutableLiveData(ShipDirection.HORIZONTAL)
    val dir: LiveData<ShipDirection> = _dir

    private val _remaining = MutableLiveData(BoardConstants.SHIP_SIZES.size)
    val remaining: LiveData<Int> = _remaining

    // Current ship size being placed (shown to user)
    private val _currentShipSize = MutableLiveData(BoardConstants.SHIP_SIZES.first())
    val currentShipSize: LiveData<Int> = _currentShipSize

    private val _submitResult = MutableLiveData<Result<Unit>>()
    val submitResult: LiveData<Result<Unit>> = _submitResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun init(id: String, p1: Boolean) {
        if (this::gameId.isInitialized) return  // already init — don't reset
        gameId    = id
        isPlayer1 = p1
        reset()
    }

    private fun reset() {
        placed.clear()
        remainingQueue.clear()
        remainingQueue.addAll(BoardConstants.SHIP_SIZES)
        _remaining.value    = remainingQueue.size
        _currentShipSize.value = remainingQueue.firstOrNull() ?: 1
        _board.value        = List(100) { 0 }
    }

    fun toggleDir() {
        _dir.value = if (_dir.value == ShipDirection.HORIZONTAL)
            ShipDirection.VERTICAL else ShipDirection.HORIZONTAL
    }

    fun placeAt(row: Int, col: Int) {
        if (remainingQueue.isEmpty()) return
        val size = remainingQueue.first()
        val ship = Ship(
            id        = placed.size,
            size      = size,
            row       = row,
            col       = col,
            direction = _dir.value!!
        )
        if (BoardValidator.canPlace(placed, ship)) {
            placed.add(ship)
            remainingQueue.removeAt(0)
            _remaining.value       = remainingQueue.size
            _currentShipSize.value = remainingQueue.firstOrNull() ?: 0
            _board.value           = BoardValidator.buildBoard(placed)
        } else {
            _error.value = "Cannot place ship here — too close to another ship or out of bounds"
        }
    }

    /** Remove last placed ship so player can redo */
    fun undoLast() {
        if (placed.isEmpty()) return
        val last = placed.removeAt(placed.size - 1)
        remainingQueue.add(0, last.size)
        _remaining.value       = remainingQueue.size
        _currentShipSize.value = remainingQueue.first()
        _board.value           = BoardValidator.buildBoard(placed)
    }

    fun autoPlace() {
        placed.clear()
        val ships = BoardValidator.autoPlace()
        placed.addAll(ships)
        remainingQueue.clear()           // all placed
        _remaining.value       = 0
        _currentShipSize.value = 0
        _board.value           = BoardValidator.buildBoard(placed)
    }

    fun submit() {
        if (placed.size < BoardConstants.SHIP_SIZES.size) {
            _error.value = "Place all ${BoardConstants.SHIP_SIZES.size} ships first"
            return
        }
        viewModelScope.launch {
            _submitResult.value = Result.Loading
            _submitResult.value = repo.submitShips(
                gameId, placed, BoardValidator.buildBoard(placed), isPlayer1
            )
        }
    }

    fun clearError() { _error.value = null }
}
