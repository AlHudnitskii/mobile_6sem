package com.example.battleship.game

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.battleship.App
import com.example.battleship.R
import com.example.battleship.common.*
import com.example.battleship.databinding.FragmentPlacementBinding
import com.google.android.material.snackbar.Snackbar

class PlacementFragment : Fragment() {
    private var _b: FragmentPlacementBinding? = null
    private val b get() = _b!!
    private val args: PlacementFragmentArgs by navArgs()
    private val vm: PlacementViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentPlacementBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm.init(args.gameId, args.isPlayer1)

        b.boardView.showShips    = true
        b.boardView.isInteractive = true
        b.boardView.cellClickListener = object : BoardView.OnCellClickListener {
            override fun onCellClick(row: Int, col: Int) { vm.placeAt(row, col) }
        }

        b.btnRotate.setOnClickListener   { vm.toggleDir() }
        b.btnAutoPlace.setOnClickListener { vm.autoPlace() }
        b.btnUndo.setOnClickListener     { vm.undoLast() }
        b.btnReady.setOnClickListener    { vm.submit() }

        vm.board.observe(viewLifecycleOwner)     { b.boardView.updateBoard(it) }

        vm.dir.observe(viewLifecycleOwner) { dir ->
            b.btnRotate.text = if (dir == ShipDirection.HORIZONTAL)
                "↔ Horizontal" else "↕ Vertical"
        }

        vm.remaining.observe(viewLifecycleOwner) { rem ->
            b.btnReady.isEnabled = rem == 0
            b.btnUndo.isEnabled  = rem < BoardConstants.SHIP_SIZES.size
        }

        vm.currentShipSize.observe(viewLifecycleOwner) { size ->
            val rem = vm.remaining.value ?: 0
            b.tvShipsRemaining.text = if (rem == 0)
                "✓ All ships placed — tap Ready!"
            else
                "Placing: ship of size $size  |  $rem remaining"
        }

        vm.submitResult.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Result.Loading -> { b.progressBar.visible(); b.btnReady.isEnabled = false }
                is Result.Success -> {
                    b.progressBar.gone()
                    findNavController().navigate(
                        PlacementFragmentDirections.actionPlacementFragmentToGameFragment(
                            args.gameId, args.isPlayer1
                        )
                    )
                }
                is Result.Error -> {
                    b.progressBar.gone()
                    b.btnReady.isEnabled = true
                    Snackbar.make(b.root, res.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        vm.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
                vm.clearError()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
