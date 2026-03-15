package com.example.timer.ui.edit

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.timer.data.db.AppDatabase
import com.example.timer.data.model.Sequence
import com.example.timer.data.repository.SequenceRepository
import com.example.timer.databinding.FragmentEditBinding
import com.example.timer.R

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: EditViewModel by viewModels {
        EditViewModelFactory(
            SequenceRepository(AppDatabase.getDatabase(requireContext()).sequenceDao())
        )
    }
    private var selectedColor: Int = 0xFF5C6BC0.toInt()

    private val colorOptions = listOf(
        0xFF5C6BC0.toInt(), 0xFFE53935.toInt(), 0xFF43A047.toInt(),
        0xFFFF8F00.toInt(), 0xFF00ACC1.toInt(), 0xFF8E24AA.toInt()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.sequenceId != -1L) viewModel.load(args.sequenceId)

        viewModel.sequence.observe(viewLifecycleOwner) { seq ->
            seq ?: return@observe
            binding.etName.setText(seq.name)
            binding.sbWarmup.progress = seq.warmupDuration
            binding.sbWork.progress = seq.workDuration
            binding.sbRest.progress = seq.restDuration
            binding.sbCooldown.progress = seq.cooldownDuration
            binding.sbCycles.progress = seq.cycles
            binding.sbRestBetween.progress = seq.restBetweenCycles
            selectedColor = seq.color
            binding.colorPreview.setBackgroundColor(selectedColor)
            refreshLabels()
        }

        setupSeekBar(binding.sbWarmup) { refreshLabels() }
        setupSeekBar(binding.sbWork) { refreshLabels() }
        setupSeekBar(binding.sbRest) { refreshLabels() }
        setupSeekBar(binding.sbCooldown) { refreshLabels() }
        setupSeekBar(binding.sbCycles) { refreshLabels() }
        setupSeekBar(binding.sbRestBetween) { refreshLabels() }

        val colorViews = listOf(binding.color1, binding.color2, binding.color3,
            binding.color4, binding.color5, binding.color6)
        colorOptions.forEachIndexed { i, c ->
            colorViews[i].setBackgroundColor(c)
            colorViews[i].setOnClickListener {
                selectedColor = c
                binding.colorPreview.setBackgroundColor(c)
            }
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim().ifEmpty { getString(R.string.no_name) }
            val seq = Sequence(
                id = if (args.sequenceId == -1L) 0L else args.sequenceId,
                name = name,
                color = selectedColor,
                warmupDuration = binding.sbWarmup.progress,
                workDuration = binding.sbWork.progress.coerceAtLeast(5),
                restDuration = binding.sbRest.progress.coerceAtLeast(1),
                cooldownDuration = binding.sbCooldown.progress,
                cycles = binding.sbCycles.progress.coerceAtLeast(1),
                restBetweenCycles = binding.sbRestBetween.progress
            )
            viewModel.save(seq)
            findNavController().navigateUp()
        }
    }

    private fun setupSeekBar(sb: SeekBar, onChange: () -> Unit) {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) = onChange()
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    private fun refreshLabels() {
        val s = getString(R.string.sec_suffix)
        binding.tvWarmup.text      = "${getString(R.string.warmup_label)}: ${binding.sbWarmup.progress}$s"
        binding.tvWork.text        = "${getString(R.string.work_label)}: ${binding.sbWork.progress}$s"
        binding.tvRest.text        = "${getString(R.string.rest_label)}: ${binding.sbRest.progress}$s"
        binding.tvCooldown.text    = "${getString(R.string.cooldown_label)}: ${binding.sbCooldown.progress}$s"
        binding.tvCycles.text      = "${getString(R.string.cycles_label)}: ${binding.sbCycles.progress}"
        binding.tvRestBetween.text = "${getString(R.string.rest_between_label)}: ${binding.sbRestBetween.progress}$s"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
