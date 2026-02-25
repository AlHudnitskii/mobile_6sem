package com.example.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class DataFragment : Fragment() {

    private lateinit var tvInput: TextView
    private lateinit var tvOutput: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var btnSwap: Button
    private lateinit var btnCopyInput: ImageButton
    private lateinit var btnCopyOutput: ImageButton

    private var inputValue = "0"
    private var selectedCategory = "Distance"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvInput = view.findViewById(R.id.tvInput)
        tvOutput = view.findViewById(R.id.tvOutput)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        spinnerFrom = view.findViewById(R.id.spinnerFrom)
        spinnerTo = view.findViewById(R.id.spinnerTo)
        btnSwap = view.findViewById(R.id.btnSwap)
        btnCopyInput = view.findViewById(R.id.btnCopyInput)
        btnCopyOutput = view.findViewById(R.id.btnCopyOutput)

        savedInstanceState?.let {
            inputValue = it.getString("inputValue", "0")!!
            selectedCategory = it.getString("selectedCategory", "Distance")!!
        }

        btnSwap.setOnClickListener { swapValues() }
        btnCopyInput.setOnClickListener { copyToClipboard(tvInput.text.toString()) }
        btnCopyOutput.setOnClickListener { copyToClipboard(tvOutput.text.toString()) }

        setupCategorySpinner()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputValue", inputValue)
        outState.putString("selectedCategory", selectedCategory)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("value", text))
        Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun swapValues() {
        val outputVal = tvOutput.text.toString()
        val fromPos = spinnerFrom.selectedItemPosition
        val toPos = spinnerTo.selectedItemPosition
        inputValue = outputVal
        tvInput.text = inputValue
        spinnerFrom.setSelection(toPos)
        spinnerTo.setSelection(fromPos)
    }

    fun onKeyPressed(key: String) {
        when (key) {
            "DEL" -> inputValue = if (inputValue.length > 1) inputValue.dropLast(1) else "0"
            "." -> if (!inputValue.contains(".")) inputValue += "."
            else -> inputValue = if (inputValue == "0") key else inputValue + key
        }
        tvInput.text = inputValue
        updateOutput()
    }

    private fun updateOutput() {
        val value = inputValue.toDoubleOrNull() ?: 0.0
        val from = spinnerFrom.selectedItem?.toString() ?: return
        val to = spinnerTo.selectedItem?.toString() ?: return
        val result = Converter.convert(value, from, to, selectedCategory)
        tvOutput.text = "%.4f".format(result)
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Converter.categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedCategory = Converter.categories[pos]
                setupUnitSpinners()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val idx = Converter.categories.indexOf(selectedCategory)
        spinnerCategory.setSelection(if (idx >= 0) idx else 0)
    }

    private fun setupUnitSpinners() {
        val units = Converter.units[selectedCategory] ?: return
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter
        if (units.size > 1) spinnerTo.setSelection(1)
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) { updateOutput() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinnerFrom.onItemSelectedListener = listener
        spinnerTo.onItemSelectedListener = listener
    }
}
