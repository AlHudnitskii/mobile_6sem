package com.example.battleship.common

import kotlin.random.Random

object BoardValidator {

    fun canPlace(existing: List<Ship>, newShip: Ship, boardSize: Int = BoardConstants.SIZE): Boolean {
        val newCells = newShip.cells()
        // Bounds check
        for ((r, c) in newCells) {
            if (r < 0 || r >= boardSize || c < 0 || c >= boardSize) return false
        }
        // Collision + buffer zone check
        val existingSet = existing.flatMap { it.cells() }.toSet()
        for ((nr, nc) in newCells) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (Pair(nr + dr, nc + dc) in existingSet) return false
                }
            }
        }
        return true
    }

    fun buildBoard(ships: List<Ship>): List<Int> {
        val board = MutableList(100) { CellState.EMPTY.ordinal }
        for (ship in ships) {
            for ((r, c) in ship.cells()) {
                board[BoardConstants.index(r, c)] = CellState.SHIP.ordinal
            }
        }
        return board
    }

    /** Generate a valid random ship placement. Guaranteed to succeed. */
    fun autoPlace(): List<Ship> {
        var attempts = 0
        while (attempts < 1000) {
            attempts++
            val placed = mutableListOf<Ship>()
            var failed = false
            for ((idx, size) in BoardConstants.SHIP_SIZES.withIndex()) {
                var placed_ok = false
                for (tries in 0 until 200) {
                    val row = Random.nextInt(BoardConstants.SIZE)
                    val col = Random.nextInt(BoardConstants.SIZE)
                    val dir = if (Random.nextBoolean()) ShipDirection.HORIZONTAL else ShipDirection.VERTICAL
                    val ship = Ship(id = idx, size = size, row = row, col = col, direction = dir)
                    if (canPlace(placed, ship)) {
                        placed.add(ship)
                        placed_ok = true
                        break
                    }
                }
                if (!placed_ok) { failed = true; break }
            }
            if (!failed) return placed
        }
        // Fallback: deterministic placement (should never reach here)
        return fallbackPlacement()
    }

    private fun fallbackPlacement(): List<Ship> {
        // Place ships in fixed positions known to be valid
        val placed = mutableListOf<Ship>()
        val positions = listOf(
            Triple(0, 0, ShipDirection.HORIZONTAL), // size 4
            Triple(2, 0, ShipDirection.HORIZONTAL), // size 3
            Triple(2, 4, ShipDirection.HORIZONTAL), // size 3
            Triple(4, 0, ShipDirection.HORIZONTAL), // size 2
            Triple(4, 3, ShipDirection.HORIZONTAL), // size 2
            Triple(4, 6, ShipDirection.HORIZONTAL), // size 2
            Triple(6, 0, ShipDirection.HORIZONTAL), // size 1
            Triple(6, 2, ShipDirection.HORIZONTAL), // size 1
            Triple(6, 4, ShipDirection.HORIZONTAL), // size 1
            Triple(6, 6, ShipDirection.HORIZONTAL), // size 1
        )
        BoardConstants.SHIP_SIZES.forEachIndexed { idx, size ->
            val (r, c, d) = positions[idx]
            placed.add(Ship(id = idx, size = size, row = r, col = c, direction = d))
        }
        return placed
    }

    fun processShot(
        ships: List<Ship>,
        board: MutableList<Int>,
        row: Int,
        col: Int
    ): Triple<List<Ship>, MutableList<Int>, Boolean> {
        val idx   = BoardConstants.index(row, col)
        val state = CellState.values()[board[idx]]
        // Already shot here
        if (state == CellState.HIT || state == CellState.MISS || state == CellState.SUNK)
            return Triple(ships, board, false)

        var hit = false
        val updatedShips = ships.map { ship ->
            val hitIdx = ship.cells().indexOfFirst { (r, c) -> r == row && c == col }
            if (hitIdx >= 0) {
                hit = true
                val newHits   = ship.hits + hitIdx
                val updated   = ship.copy(hits = newHits)
                if (updated.isSunk) {
                    // Mark all cells of sunk ship
                    for ((r, c) in ship.cells())
                        board[BoardConstants.index(r, c)] = CellState.SUNK.ordinal
                } else {
                    board[idx] = CellState.HIT.ordinal
                }
                updated
            } else ship
        }

        if (!hit) board[idx] = CellState.MISS.ordinal
        return Triple(updatedShips, board, hit)
    }

    fun allSunk(ships: List<Ship>) = ships.isNotEmpty() && ships.all { it.isSunk }
}
