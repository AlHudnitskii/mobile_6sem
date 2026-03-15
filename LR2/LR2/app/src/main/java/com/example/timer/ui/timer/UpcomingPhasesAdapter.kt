package com.example.timer.ui.timer

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timer.R
import com.example.timer.data.model.Phase
import com.example.timer.data.model.PhaseType
import com.example.timer.databinding.ItemPhaseBinding

class UpcomingPhasesAdapter : ListAdapter<Phase, UpcomingPhasesAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemPhaseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(phase: Phase) {
            val labelRes = when (phase.type) {
                PhaseType.WARMUP   -> R.string.warmup_label
                PhaseType.WORK     -> R.string.work_label
                PhaseType.REST     -> R.string.rest_label
                PhaseType.COOLDOWN -> R.string.cooldown_label
            }
            val label = b.root.context.getString(labelRes)
            b.tvPhaseLabel.text = if (phase.cycleNumber > 0) "$label (${phase.cycleNumber})" else label
            b.tvPhaseDuration.text = "%02d:%02d".format(phase.durationSeconds / 60, phase.durationSeconds % 60)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemPhaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Phase>() {
            override fun areItemsTheSame(a: Phase, b: Phase) =
                a.type == b.type && a.cycleNumber == b.cycleNumber
            override fun areContentsTheSame(a: Phase, b: Phase) = a == b
        }
    }
}