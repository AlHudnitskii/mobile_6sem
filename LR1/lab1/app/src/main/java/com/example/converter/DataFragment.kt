package com.example.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment

class DataFragment : Fragment() {

    private lateinit var tvInput: TextView
    private lateinit var tvOutput: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner

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

        savedInstanceState?.let {
            inputValue = it.getString("inputValue", "0")!!
            selectedCategory = it.getString("selectedCategory", "Distance")!!
        }
        setupCategorySpinner()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputValue", inputValue)
        outState.putString("selectedCategory", selectedCategory)
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
