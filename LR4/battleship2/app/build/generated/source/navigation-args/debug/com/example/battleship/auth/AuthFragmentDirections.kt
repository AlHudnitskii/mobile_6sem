package com.example.battleship.auth

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.battleship.R

public class AuthFragmentDirections private constructor() {
  public companion object {
    public fun actionAuthFragmentToLobbyFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_authFragment_to_lobbyFragment)
  }
}
