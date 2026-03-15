package com.example.timer.ui.timer

import android.content.*
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timer.R
import com.example.timer.data.db.AppDatabase
import com.example.timer.data.model.Phase
import com.example.timer.data.model.PhaseType
import com.example.timer.data.model.buildPhases
import com.example.timer.databinding.FragmentTimerBinding
import com.example.timer.service.TimerService
import com.google.gson.Gson
import kotlinx.coroutines.*

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val args: TimerFragmentArgs by navArgs()
    private var phases: List<Phase> = emptyList()
    private var currentIndex = 0
    private var isPaused = false
    private lateinit var upcomingAdapter: UpcomingPhasesAdapter

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                TimerService.BROADCAST_TICK -> {
                    val rem = intent.getIntExtra(TimerService.EXTRA_REMAINING, 0)
                    binding.tvTime.text = formatTime(rem)
                }
                TimerService.BROADCAST_PHASE_CHANGE -> {
                    currentIndex = intent.getIntExtra(TimerService.EXTRA_PHASE_INDEX, 0)
                    updatePhaseUI()
                }
                TimerService.BROADCAST_FINISHED -> {
                    if (isAdded) findNavController().navigateUp()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upcomingAdapter = UpcomingPhasesAdapter()
        binding.rvUpcoming.apply {
            adapter = upcomingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        CoroutineScope(Dispatchers.IO).launch {
            val seq = AppDatabase.getDatabase(requireContext()).sequenceDao().getById(args.sequenceId)
            seq?.let {
                phases = it.buildPhases()
                val json = Gson().toJson(phases)
                withContext(Dispatchers.Main) {
                    updatePhaseUI()
                    startTimerService(json)
                }
            }
        }

        binding.btnPause.setOnClickListener {
            if (isPaused) {
                sendAction(TimerService.ACTION_RESUME)
                binding.btnPause.setImageResource(android.R.drawable.ic_media_pause)
                isPaused = false
            } else {
                sendAction(TimerService.ACTION_PAUSE)
                binding.btnPause.setImageResource(android.R.drawable.ic_media_play)
                isPaused = true
            }
        }
        binding.btnNext.setOnClickListener { sendAction(TimerService.ACTION_NEXT) }
        binding.btnPrev.setOnClickListener { sendAction(TimerService.ACTION_PREV) }
        binding.btnStop.setOnClickListener {
            sendAction(TimerService.ACTION_STOP)
            findNavController().navigateUp()
        }
    }

    private fun updatePhaseUI() {
        if (phases.isEmpty()) return
        val phase = phases.getOrNull(currentIndex) ?: return

        binding.tvPhaseLabel.text = when (phase.type) {
            PhaseType.WARMUP   -> getString(R.string.warmup_label)
            PhaseType.WORK     -> getString(R.string.work_label)
            PhaseType.REST     -> getString(R.string.rest_label)
            PhaseType.COOLDOWN -> getString(R.string.cooldown_label)
        }

        if (phase.cycleNumber > 0) {
            binding.tvCycleInfo.text = "${phase.cycleNumber}/${phases.count { it.type == PhaseType.WORK }}"
            binding.tvCycleInfo.visibility = View.VISIBLE
        } else {
            binding.tvCycleInfo.visibility = View.GONE
        }
        upcomingAdapter.submitList(phases.drop(currentIndex + 1))
    }

    private fun startTimerService(json: String) {
        requireContext().startForegroundService(
            Intent(requireContext(), TimerService::class.java).apply {
                action = TimerService.ACTION_START
                putExtra(TimerService.EXTRA_PHASES, json)
            }
        )
    }

    private fun sendAction(action: String) {
        requireContext().startService(
            Intent(requireContext(), TimerService::class.java).apply { this.action = action }
        )
    }

    private fun formatTime(s: Int) = "%02d:%02d".format(s / 60, s % 60)

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(TimerService.BROADCAST_TICK)
            addAction(TimerService.BROADCAST_PHASE_CHANGE)
            addAction(TimerService.BROADCAST_FINISHED)
        }
        requireContext().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        try { requireContext().unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}