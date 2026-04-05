package com.example.battleship.game

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.battleship.R

public class GameFragmentDirections private constructor() {
  public companion object {
    public fun actionGameFragmentToLobbyFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_gameFragment_to_lobbyFragment)
  }
}
