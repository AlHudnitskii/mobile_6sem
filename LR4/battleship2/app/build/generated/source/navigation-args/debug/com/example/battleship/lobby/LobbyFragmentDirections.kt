package com.example.battleship.lobby

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.battleship.R
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public class LobbyFragmentDirections private constructor() {
  private data class ActionLobbyFragmentToPlacementFragment(
    public val gameId: String,
    public val isPlayer1: Boolean,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_lobbyFragment_to_placementFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("gameId", this.gameId)
        result.putBoolean("isPlayer1", this.isPlayer1)
        return result
      }
  }

  public companion object {
    public fun actionLobbyFragmentToPlacementFragment(gameId: String, isPlayer1: Boolean):
        NavDirections = ActionLobbyFragmentToPlacementFragment(gameId, isPlayer1)
  }
}
