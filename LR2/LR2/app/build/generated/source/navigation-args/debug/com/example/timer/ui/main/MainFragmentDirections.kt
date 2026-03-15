package com.example.timer.ui.main

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.timer.R
import kotlin.Int
import kotlin.Long

public class MainFragmentDirections private constructor() {
  private data class ActionMainFragmentToTimerFragment(
    public val sequenceId: Long,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_mainFragment_to_timerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("sequenceId", this.sequenceId)
        return result
      }
  }

  private data class ActionMainFragmentToEditFragment(
    public val sequenceId: Long = -1L,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_mainFragment_to_editFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("sequenceId", this.sequenceId)
        return result
      }
  }

  public companion object {
    public fun actionMainFragmentToTimerFragment(sequenceId: Long): NavDirections =
        ActionMainFragmentToTimerFragment(sequenceId)

    public fun actionMainFragmentToEditFragment(sequenceId: Long = -1L): NavDirections =
        ActionMainFragmentToEditFragment(sequenceId)

    public fun actionMainFragmentToSettingsFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_mainFragment_to_settingsFragment)
  }
}
