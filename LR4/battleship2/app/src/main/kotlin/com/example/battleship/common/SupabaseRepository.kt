package com.example.battleship.common

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.math.max

class SupabaseRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
    private val client = SupabaseClient.http
    private val json   = SupabaseClient.json

    // ─── Token ────────────────────────────────────────────────────────────────

    var accessToken: String
        get()      = prefs.getString("access_token", "") ?: ""
        private set(v) { prefs.edit().putString("access_token", v).apply() }

    var currentUserId: String
        get()      = prefs.getString("user_id", "") ?: ""
        private set(v) { prefs.edit().putString("user_id", v).apply() }

    val isLoggedIn: Boolean get() = accessToken.isNotBlank()
    private fun auth() = "Bearer $accessToken"

    // ─── Auth ─────────────────────────────────────────────────────────────────

    suspend fun signUp(email: String, password: String, nickname: String): Result<Unit> = try {
        val resp: AuthResponse = client.post(SupabaseClient.auth("signup")) {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password"}""")
        }.body()

        if (resp.accessToken.isBlank()) {
            Result.Error(resp.errorDescription ?: resp.msg ?: "Sign-up failed")
        } else {
            accessToken = resp.accessToken
            currentUserId = resp.user?.id ?: ""

            client.post(SupabaseClient.rest("profiles")) {
                header("Authorization", auth())
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody("""{"id":"$currentUserId","email":"$email","nickname":"$nickname","avatar_index":0,"avatar_url":"","total_games":0,"wins":0,"losses":0}""")
            }
            Result.Success(Unit)
        }
    } catch (e: Exception) {
        Log.e("Auth", "signUp error", e)
        Result.Error(e.localizedMessage ?: "Sign-up failed")
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        val resp: AuthResponse = client.post(SupabaseClient.auth("token?grant_type=password")) {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password"}""")
        }.body()

        if (resp.accessToken.isBlank()) {
            Result.Error(resp.errorDescription ?: resp.msg ?: "Sign-in failed")
        } else {
            accessToken = resp.accessToken
            currentUserId = resp.user?.id ?: ""
            Result.Success(Unit)
        }
    } catch (e: Exception) {
        Log.e("Auth", "signIn error", e)
        Result.Error(e.localizedMessage ?: "Sign-in failed")
    }

    fun signOut() { prefs.edit().clear().apply() }

    // ─── Profile ──────────────────────────────────────────────────────────────

    suspend fun getProfile(uid: String = currentUserId): Result<UserProfile> = try {
        val list: List<UserProfile> = client.get(SupabaseClient.rest("profiles")) {
            header("Authorization", auth())
            parameter("id", "eq.$uid")
            parameter("select", "*")
            parameter("limit", "1")
        }.body()
        if (list.isEmpty()) Result.Error("Profile not found")
        else Result.Success(list.first())
    } catch (e: Exception) {
        Log.e("Profile", "getProfile error", e)
        Result.Error(e.localizedMessage ?: "Failed to load profile")
    }

    suspend fun updateNickname(nick: String): Result<Unit> = try {
        client.patch(SupabaseClient.rest("profiles")) {
            header("Authorization", auth())
            header("Prefer", "return=minimal")
            parameter("id", "eq.$currentUserId")
            contentType(ContentType.Application.Json)
            setBody("""{"nickname":"$nick"}""")
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Update failed")
    }

    suspend fun updateAvatarIndex(index: Int): Result<Unit> = try {
        client.patch(SupabaseClient.rest("profiles")) {
            header("Authorization", auth())
            header("Prefer", "return=minimal")
            parameter("id", "eq.$currentUserId")
            contentType(ContentType.Application.Json)
            setBody("""{"avatar_index":$index,"avatar_url":""}""")
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Update failed")
    }

    suspend fun uploadAvatar(context: Context, uri: Uri): Result<String> = try {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }

        if (bytes == null) {
            Result.Error("Could not read image")
        } else {
            val compressed = compressToJpeg(bytes)
            val storagePath = "$currentUserId/avatar.jpg"

            val uploadResp = client.put(SupabaseClient.storage("avatars", storagePath)) {
                header("Authorization", auth())
                header("x-upsert", "true")
                contentType(ContentType.Image.JPEG)
                setBody(compressed)
            }

            if (uploadResp.status.value !in 200..299) {
                val errBody = uploadResp.bodyAsText()
                Result.Error("Upload failed: ${uploadResp.status} $errBody")
            } else {
                val publicUrl = SupabaseClient.storagePublic("avatars", storagePath)
                client.patch(SupabaseClient.rest("profiles")) {
                    header("Authorization", auth())
                    header("Prefer", "return=minimal")
                    parameter("id", "eq.$currentUserId")
                    contentType(ContentType.Application.Json)
                    setBody("""{"avatar_url":"$publicUrl","avatar_index":-1}""")
                }
                Result.Success(publicUrl)
            }
        }
    } catch (e: Exception) {
        Log.e("Avatar", "upload error", e)
        Result.Error(e.localizedMessage ?: "Upload failed")
    }

    private fun compressToJpeg(data: ByteArray, maxBytes: Int = 150_000): ByteArray {
        try {
            val bm = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
            if (bm == null) return data

            val maxDim = 400
            val width = bm.width
            val height = bm.height

            val scale = if (width > maxDim || height > maxDim) {
                maxDim.toFloat() / max(width, height)
            } else {
                1f
            }

            val scaled = if (scale < 1f) {
                android.graphics.Bitmap.createScaledBitmap(
                    bm,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                bm
            }

            var quality = 80
            var result: ByteArray
            do {
                val out = java.io.ByteArrayOutputStream()
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
                result = out.toByteArray()
                quality -= 10
            } while (result.size > maxBytes && quality > 10)

            if (scaled !== bm) scaled.recycle()
            bm.recycle()
            return result
        } catch (e: Exception) {
            Log.e("Avatar", "Compression error", e)
            return data
        }
    }
    suspend fun createGame(nickname: String): Result<String> = try {
        val gameId = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
        val emptyBoard = List(100) { 0 }
        val boardJson = json.encodeToString(emptyBoard)
        val body = """
    {
      "id":"$gameId",
      "player1_id":"$currentUserId",
      "player2_id":null,
      "player1_nickname":"$nickname",
      "player2_nickname":"",
      "status":"WAITING_FOR_PLAYER",
      "player1_board":$boardJson,
      "player2_board":$boardJson,
      "player1_ships":"[]",
      "player2_ships":"[]",
      "player1_ready":false,
      "player2_ready":false,
      "winner_id":null
    }
    """.trimIndent()

        val resp = client.post(SupabaseClient.rest("games")) {
            header("Authorization", auth())
            header("Prefer", "return=minimal")
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        if (resp.status.value !in 200..299) {
            val err = resp.bodyAsText()
            Log.e("Game", "createGame failed: ${resp.status} $err")
            Result.Error("Create failed: ${resp.status.value} $err")
        } else {
            Result.Success(gameId)
        }
    } catch (e: Exception) {
        Log.e("Game", "createGame error", e)
        Result.Error(e.localizedMessage ?: "Create game failed")
    }

    suspend fun joinGame(gameId: String, nickname: String): Result<GameRow> = try {
        val list: List<GameRow> = client.get(SupabaseClient.rest("games")) {
            header("Authorization", auth())
            parameter("id", "eq.$gameId")
            parameter("select", "*")
            parameter("limit", "1")
        }.body()

        val game = list.firstOrNull()

        if (game == null) {
            Result.Error("Game not found")
        } else if (game.gameStatus() != GameStatus.WAITING_FOR_PLAYER) {
            Result.Error("Game is not available (status: ${game.status})")
        } else if (game.player1Id == currentUserId) {
            Result.Error("Cannot join your own game")
        } else {
            val resp = client.patch(SupabaseClient.rest("games")) {
                header("Authorization", auth())
                header("Prefer", "return=minimal")
                parameter("id", "eq.$gameId")
                contentType(ContentType.Application.Json)
                setBody("""{"player2_id":"$currentUserId","player2_nickname":"$nickname","status":"PLACEMENT"}""")
            }

            if (resp.status.value !in 200..299) {
                Result.Error("Join failed: ${resp.status.value}")
            } else {
                Result.Success(game.copy(player2Id = currentUserId, player2Nickname = nickname, status = "PLACEMENT"))
            }
        }
    } catch (e: Exception) {
        Log.e("Game", "joinGame error", e)
        Result.Error(e.localizedMessage ?: "Join failed")
    }

    // FIX: submitShips — use raw JSON to avoid Any serialization issues
    suspend fun submitShips(
        gameId: String, ships: List<Ship>, board: List<Int>, isPlayer1: Boolean
    ): Result<Unit> = try {
        val shipsJson = json.encodeToString(ships)
        val boardJson = json.encodeToString(board)
        val boardField  = if (isPlayer1) "player1_board"  else "player2_board"
        val shipsField  = if (isPlayer1) "player1_ships"  else "player2_ships"
        val readyField  = if (isPlayer1) "player1_ready"  else "player2_ready"

        val body = """{"$boardField":$boardJson,"$shipsField":$shipsJson,"$readyField":true}"""
        val resp = client.patch(SupabaseClient.rest("games")) {
            header("Authorization", auth())
            header("Prefer", "return=minimal")
            parameter("id", "eq.$gameId")
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        if (resp.status.value !in 200..299) {
            Result.Error("Submit ships failed: ${resp.status.value}")
        } else {
            // Check both ready → start
            val list: List<JsonObject> = client.get(SupabaseClient.rest("games")) {
                header("Authorization", auth())
                parameter("id", "eq.$gameId")
                parameter("select", "player1_ready,player2_ready")
            }.body()
            val g = list.firstOrNull()
            val p1r = g?.get("player1_ready")?.jsonPrimitive?.booleanOrNull ?: false
            val p2r = g?.get("player2_ready")?.jsonPrimitive?.booleanOrNull ?: false
            if (p1r && p2r) {
                client.patch(SupabaseClient.rest("games")) {
                    header("Authorization", auth())
                    header("Prefer", "return=minimal")
                    parameter("id", "eq.$gameId")
                    contentType(ContentType.Application.Json)
                    setBody("""{"status":"PLAYER1_TURN"}""")
                }
            }
            Result.Success(Unit)
        }
    } catch (e: Exception) {
        Log.e("Game", "submitShips error", e)
        Result.Error(e.localizedMessage ?: "Submit ships failed")
    }

    suspend fun fireShot(
        gameId: String, row: Int, col: Int, isPlayer1: Boolean, currentGame: GameRow
    ): Result<Unit> = try {
        val targetShipsJson = if (isPlayer1) currentGame.player2Ships else currentGame.player1Ships
        val targetShips = json.decodeFromString<List<Ship>>(targetShipsJson)
        val targetBoard = (if (isPlayer1) currentGame.player2Board else currentGame.player1Board).toMutableList()

        val (updatedShips, updatedBoard, _) = BoardValidator.processShot(targetShips, targetBoard, row, col)
        val allSunk = BoardValidator.allSunk(updatedShips)

        val boardField = if (isPlayer1) "player2_board" else "player1_board"
        val shipsField = if (isPlayer1) "player2_ships" else "player1_ships"
        val nextStatus = when {
            allSunk   -> "FINISHED"
            isPlayer1 -> "PLAYER2_TURN"
            else      -> "PLAYER1_TURN"
        }
        val boardJson  = json.encodeToString(updatedBoard)
        val shipsJsonStr = json.encodeToString(updatedShips)
        val winnerPart = if (allSunk) ""","winner_id":"$currentUserId"""" else ""
        val body = """{"$boardField":$boardJson,"$shipsField":$shipsJsonStr,"status":"$nextStatus"$winnerPart}"""

        client.patch(SupabaseClient.rest("games")) {
            header("Authorization", auth())
            header("Prefer", "return=minimal")
            parameter("id", "eq.$gameId")
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        if (allSunk) saveGameRecords(gameId, currentGame)
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("Game", "fireShot error", e)
        Result.Error(e.localizedMessage ?: "Shot failed")
    }

    private suspend fun saveGameRecords(gameId: String, game: GameRow) {
        try {
            val loserId = if (game.player1Id == currentUserId) game.player2Id else game.player1Id
            val loserNick = if (game.player1Id == currentUserId) game.player2Nickname else game.player1Nickname
            val winNick = if (game.player1Id == currentUserId) game.player1Nickname else game.player2Nickname

            // Проверяем, что loserId не null и не пустой
            if (!loserId.isNullOrBlank()) {
                client.post(SupabaseClient.rest("game_records")) {
                    header("Authorization", auth())
                    header("Prefer", "return=minimal")
                    contentType(ContentType.Application.Json)
                    setBody("""[
                    {"user_id":"$currentUserId","game_id":"$gameId","opponent_nickname":"$loserNick","result":"WIN"},
                    {"user_id":"$loserId","game_id":"$gameId","opponent_nickname":"$winNick","result":"LOSS"}
                ]""")
                }
            } else {
                // Если нет проигравшего (например, игра с ботом), сохраняем только победителя
                client.post(SupabaseClient.rest("game_records")) {
                    header("Authorization", auth())
                    header("Prefer", "return=minimal")
                    contentType(ContentType.Application.Json)
                    setBody("""[
                    {"user_id":"$currentUserId","game_id":"$gameId","opponent_nickname":"$winNick","result":"WIN"}
                ]""")
                }
            }

            // update winner stats
            val wProf = (getProfile(currentUserId) as? Result.Success)?.data
            wProf?.let {
                client.patch(SupabaseClient.rest("profiles")) {
                    header("Authorization", auth())
                    header("Prefer", "return=minimal")
                    parameter("id", "eq.$currentUserId")
                    contentType(ContentType.Application.Json)
                    setBody("""{"total_games":${it.totalGames + 1},"wins":${it.wins + 1}}""")
                }
            }

            // update loser stats - только если loserId не null и не пустой
            if (!loserId.isNullOrBlank()) {
                val lProf = (getProfile(loserId) as? Result.Success)?.data
                lProf?.let {
                    client.patch(SupabaseClient.rest("profiles")) {
                        header("Authorization", auth())
                        header("Prefer", "return=minimal")
                        parameter("id", "eq.$loserId")
                        contentType(ContentType.Application.Json)
                        setBody("""{"total_games":${it.totalGames + 1},"losses":${it.losses + 1}}""")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Stats", "saveRecords failed", e)
        }
    }

    // ─── Realtime: polling-first approach (WebSocket as bonus) ────────────────

    fun observeGame(gameId: String): Flow<GameRow?> = callbackFlow {
        // Initial fetch
        try {
            val list: List<GameRow> = client.get(SupabaseClient.rest("games")) {
                header("Authorization", auth())
                parameter("id", "eq.$gameId")
                parameter("select", "*")
            }.body()
            list.firstOrNull()?.let { trySend(it) }
        } catch (e: Exception) {
            Log.e("Game", "initial fetch failed", e)
        }

        // Polling loop (simpler, more reliable)
        val pollJob = launch {
            while (true) {
                delay(2500)
                try {
                    val list: List<GameRow> = client.get(SupabaseClient.rest("games")) {
                        header("Authorization", auth())
                        parameter("id", "eq.$gameId")
                        parameter("select", "*")
                    }.body()
                    list.firstOrNull()?.let { trySend(it) }
                } catch (ex: Exception) {
                    Log.e("Poll", "poll failed", ex)
                }
            }
        }

        awaitClose { pollJob.cancel() }
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    suspend fun getGameHistory(): Result<List<GameRecord>> = try {
        val list: List<GameRecord> = client.get(SupabaseClient.rest("game_records")) {
            header("Authorization", auth())
            parameter("user_id", "eq.$currentUserId")
            parameter("order", "created_at.desc")
            parameter("limit", "50")
        }.body()
        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Failed to load history")
    }
}
