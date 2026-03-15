package com.example.timer.ui.main

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.timer.R
import com.example.timer.data.db.AppDatabase
import com.example.timer.data.repository.SequenceRepository
import com.example.timer.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            SequenceRepository(AppDatabase.getDatabase(requireContext()).sequenceDao())
        )
    }

    private lateinit var adapter: SequenceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SequenceAdapter(
            onStart = { seq ->
                val action = MainFragmentDirections.actionMainFragmentToTimerFragment(seq.id)
                findNavController().navigate(action)
            },
            onEdit = { seq ->
                val action = MainFragmentDirections.actionMainFragmentToEditFragment(seq.id)
                findNavController().navigate(action)
            },
            onDelete = { seq -> viewModel.delete(seq) }
        )
        binding.recyclerView.adapter = adapter

        viewModel.sequences.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(-1L)
            findNavController().navigate(action)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_main, menu)
            }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
