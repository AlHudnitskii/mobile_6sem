package com.example.battleship.game

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Boolean
import kotlin.String
import kotlin.jvm.JvmStatic

public data class GameFragmentArgs(
  public val gameId: String,
  public val isPlayer1: Boolean,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("gameId", this.gameId)
    result.putBoolean("isPlayer1", this.isPlayer1)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("gameId", this.gameId)
    result.set("isPlayer1", this.isPlayer1)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): GameFragmentArgs {
      bundle.setClassLoader(GameFragmentArgs::class.java.classLoader)
      val __gameId : String?
      if (bundle.containsKey("gameId")) {
        __gameId = bundle.getString("gameId")
        if (__gameId == null) {
          throw IllegalArgumentException("Argument \"gameId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"gameId\" is missing and does not have an android:defaultValue")
      }
      val __isPlayer1 : Boolean
      if (bundle.containsKey("isPlayer1")) {
        __isPlayer1 = bundle.getBoolean("isPlayer1")
      } else {
        throw IllegalArgumentException("Required argument \"isPlayer1\" is missing and does not have an android:defaultValue")
      }
      return GameFragmentArgs(__gameId, __isPlayer1)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): GameFragmentArgs {
      val __gameId : String?
      if (savedStateHandle.contains("gameId")) {
        __gameId = savedStateHandle["gameId"]
        if (__gameId == null) {
          throw IllegalArgumentException("Argument \"gameId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"gameId\" is missing and does not have an android:defaultValue")
      }
      val __isPlayer1 : Boolean?
      if (savedStateHandle.contains("isPlayer1")) {
        __isPlayer1 = savedStateHandle["isPlayer1"]
        if (__isPlayer1 == null) {
          throw IllegalArgumentException("Argument \"isPlayer1\" of type boolean does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"isPlayer1\" is missing and does not have an android:defaultValue")
      }
      return GameFragmentArgs(__gameId, __isPlayer1)
    }
  }
}
