package com.example.timer.ui.timer

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Long
import kotlin.jvm.JvmStatic

public data class TimerFragmentArgs(
  public val sequenceId: Long,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putLong("sequenceId", this.sequenceId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("sequenceId", this.sequenceId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): TimerFragmentArgs {
      bundle.setClassLoader(TimerFragmentArgs::class.java.classLoader)
      val __sequenceId : Long
      if (bundle.containsKey("sequenceId")) {
        __sequenceId = bundle.getLong("sequenceId")
      } else {
        throw IllegalArgumentException("Required argument \"sequenceId\" is missing and does not have an android:defaultValue")
      }
      return TimerFragmentArgs(__sequenceId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): TimerFragmentArgs {
      val __sequenceId : Long?
      if (savedStateHandle.contains("sequenceId")) {
        __sequenceId = savedStateHandle["sequenceId"]
        if (__sequenceId == null) {
          throw IllegalArgumentException("Argument \"sequenceId\" of type long does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"sequenceId\" is missing and does not have an android:defaultValue")
      }
      return TimerFragmentArgs(__sequenceId)
    }
  }
}
