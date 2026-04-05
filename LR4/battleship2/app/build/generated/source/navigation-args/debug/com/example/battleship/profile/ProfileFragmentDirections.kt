package com.example.battleship.profile

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.battleship.R

public class ProfileFragmentDirections private constructor() {
  public companion object {
    public fun actionProfileFragmentToAuthFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_profileFragment_to_authFragment)
  }
}
