package com.example.battleship.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class CellState { EMPTY, SHIP, HIT, MISS, SUNK }
enum class GameStatus { WAITING_FOR_PLAYER, PLACEMENT, PLAYER1_TURN, PLAYER2_TURN, FINISHED }
enum class ShipDirection { HORIZONTAL, VERTICAL }

@Serializable
data class AuthResponse(
    @SerialName("access_token")       val accessToken: String = "",
    @SerialName("refresh_token")      val refreshToken: String = "",
    val user: AuthUser? = null,
    val error: String? = null,
    @SerialName("error_description")  val errorDescription: String? = null,
    val msg: String? = null
)

@Serializable
data class AuthUser(val id: String = "", val email: String = "")

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    val nickname: String = "",
    @SerialName("avatar_index") val avatarIndex: Int = 0,
    @SerialName("avatar_url")   val avatarUrl: String = "",
    @SerialName("total_games")  val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0
)

@Serializable
data class GameRow(
    val id: String = "",
    @SerialName("player1_id")       val player1Id: String = "",
    @SerialName("player2_id")       val player2Id: String? = null,   // nullable!
    @SerialName("player1_nickname") val player1Nickname: String = "",
    @SerialName("player2_nickname") val player2Nickname: String = "",
    val status: String = GameStatus.WAITING_FOR_PLAYER.name,
    @SerialName("player1_board")    val player1Board: List<Int> = List(100) { 0 },
    @SerialName("player2_board")    val player2Board: List<Int> = List(100) { 0 },
    @SerialName("player1_ships")    val player1Ships: String = "[]",
    @SerialName("player2_ships")    val player2Ships: String = "[]",
    @SerialName("player1_ready")    val player1Ready: Boolean = false,
    @SerialName("player2_ready")    val player2Ready: Boolean = false,
    @SerialName("winner_id")        val winnerId: String? = null,     // nullable!
    @SerialName("created_at")       val createdAt: String = ""
)

fun GameRow.gameStatus(): GameStatus = try {
    GameStatus.valueOf(status)
} catch (_: IllegalArgumentException) {
    GameStatus.WAITING_FOR_PLAYER
}

@Serializable
data class GameRecord(
    val id: String = "",
    @SerialName("user_id")            val userId: String = "",
    @SerialName("game_id")            val gameId: String = "",
    @SerialName("opponent_nickname")  val opponentNickname: String = "",
    val result: String = "",
    @SerialName("created_at")         val createdAt: String = ""
)

@Serializable
data class Ship(
    val id: Int = 0,
    val size: Int = 0,
    val row: Int = 0,
    val col: Int = 0,
    val direction: ShipDirection = ShipDirection.HORIZONTAL,
    val hits: List<Int> = emptyList()
) {
    val isSunk: Boolean get() = hits.size == size

    fun cells(): List<Pair<Int, Int>> = (0 until size).map { i ->
        if (direction == ShipDirection.HORIZONTAL) Pair(row, col + i)
        else Pair(row + i, col)
    }
}

object BoardConstants {
    const val SIZE = 10
    val SHIP_SIZES = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
    fun index(row: Int, col: Int) = row * SIZE + col
    fun row(index: Int) = index / SIZE
    fun col(index: Int) = index % SIZE
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
