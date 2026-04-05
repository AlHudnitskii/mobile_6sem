package com.example.battleship.common

import android.util.Log
import com.example.battleship.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

object SupabaseClient {

    val url: String get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    val anonKey: String get() = BuildConfig.SUPABASE_ANON_KEY

    val wsUrl: String get() = url
        .replace("https://", "wss://")
        .replace("http://", "ws://") + "/realtime/v1/websocket"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient          = true
        encodeDefaults     = true
        coerceInputValues  = true   // FIX: handle null → default for non-nullable fields
    }

    val http: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) { json(json) }
        install(WebSockets) {
            pingInterval = 30_000  // keep-alive
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) { Log.d("Ktor", message) }
            }
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis  = 30_000
            connectTimeoutMillis  = 15_000
            socketTimeoutMillis   = 30_000
        }
        defaultRequest {
            url(this@SupabaseClient.url + "/")
            headers.append("apikey", anonKey)
        }
    }

    fun rest(table: String)                            = "$url/rest/v1/$table"
    fun auth(path: String)                             = "$url/auth/v1/$path"
    fun storage(bucket: String, path: String)          = "$url/storage/v1/object/$bucket/$path"
    fun storagePublic(bucket: String, path: String)    = "$url/storage/v1/object/public/$bucket/$path"
}
