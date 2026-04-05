package com.example.battleship.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.example.battleship.App
import com.example.battleship.R
import com.example.battleship.common.*
import com.example.battleship.databinding.FragmentStatsBinding
import com.example.battleship.databinding.ItemGameRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class StatsFragment : Fragment() {
    private var _b: FragmentStatsBinding? = null
    private val b get() = _b!!
    private val vm: StatsViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentStatsBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        vm.load()
        vm.result.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Result.Loading -> { b.progressBar.visible(); b.tvEmpty.gone(); b.recyclerView.gone() }
                is Result.Success -> {
                    b.progressBar.gone()
                    if (res.data.isEmpty()) {
                        b.tvEmpty.visible(); b.recyclerView.gone()
                    } else {
                        b.tvEmpty.gone(); b.recyclerView.visible()
                        b.recyclerView.adapter = GameHistoryAdapter(res.data)
                    }
                }
                is Result.Error -> {
                    b.progressBar.gone()
                    b.tvEmpty.visible()
                    b.tvEmpty.text = res.message
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

class GameHistoryAdapter(private val items: List<GameRecord>) :
    RecyclerView.Adapter<GameHistoryAdapter.VH>() {

    private val sdf    = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    inner class VH(val b: ItemGameRecordBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemGameRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r   = items[pos]
        val win = r.result == "WIN"

        h.b.tvResult.text = if (win) "WIN 🏆" else "LOSS 💀"
        h.b.tvResult.setTextColor(
            h.itemView.context.getColor(if (win) R.color.win_color else R.color.loss_color)
        )
        h.b.tvOpponent.text = "vs ${r.opponentNickname}"
        h.b.tvDate.text = try {
            val raw = r.createdAt.substringBefore(".")
            sdf.format(parser.parse(raw) ?: Date())
        } catch (_: Exception) { r.createdAt.take(10) }
    }
}
