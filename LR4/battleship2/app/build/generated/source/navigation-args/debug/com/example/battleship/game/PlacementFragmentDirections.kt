package com.example.battleship.game

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.battleship.R
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public class PlacementFragmentDirections private constructor() {
  private data class ActionPlacementFragmentToGameFragment(
    public val gameId: String,
    public val isPlayer1: Boolean,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_placementFragment_to_gameFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("gameId", this.gameId)
        result.putBoolean("isPlayer1", this.isPlayer1)
        return result
      }
  }

  public companion object {
    public fun actionPlacementFragmentToGameFragment(gameId: String, isPlayer1: Boolean):
        NavDirections = ActionPlacementFragmentToGameFragment(gameId, isPlayer1)
  }
}
