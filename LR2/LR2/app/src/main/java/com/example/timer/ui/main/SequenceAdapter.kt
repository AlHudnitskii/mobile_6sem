package com.example.timer.ui.main

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timer.data.model.Sequence
import com.example.timer.databinding.ItemSequenceBinding

class SequenceAdapter(
    private val onStart: (Sequence) -> Unit,
    private val onEdit: (Sequence) -> Unit,
    private val onDelete: (Sequence) -> Unit
) : ListAdapter<Sequence, SequenceAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemSequenceBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(seq: Sequence) {
            b.tvName.text = seq.name
            b.colorBar.setBackgroundColor(seq.color)
            b.tvInfo.text = "${seq.cycles} цикл. · Работа: ${seq.workDuration}с · Отдых: ${seq.restDuration}с"
            b.btnStart.setOnClickListener { onStart(seq) }
            b.btnEdit.setOnClickListener { onEdit(seq) }
            b.btnDelete.setOnClickListener { onDelete(seq) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSequenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Sequence>() {
            override fun areItemsTheSame(a: Sequence, b: Sequence) = a.id == b.id
            override fun areContentsTheSame(a: Sequence, b: Sequence) = a == b
        }
    }
}
