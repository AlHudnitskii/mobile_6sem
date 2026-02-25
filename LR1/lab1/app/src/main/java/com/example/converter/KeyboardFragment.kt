package com.example.converter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class KeyboardFragment : Fragment() {

    interface OnKeyPressListener {
        fun onKeyPressed(key: String)
    }

    private var listener: OnKeyPressListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnKeyPressListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_keyboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "DEL")
        val buttonIds = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btn0, R.id.btnDot, R.id.btnDel
        )
        keys.forEachIndexed { index, key ->
            view.findViewById<Button>(buttonIds[index]).setOnClickListener {
                listener?.onKeyPressed(key)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
