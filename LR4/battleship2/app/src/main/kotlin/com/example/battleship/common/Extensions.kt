package com.example.battleship.common

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.visible()   { visibility = View.VISIBLE }
fun View.gone()      { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.snack(msg: String, len: Int = Snackbar.LENGTH_SHORT) =
    Snackbar.make(this, msg, len).show()
