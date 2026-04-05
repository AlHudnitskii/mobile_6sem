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
import com.example.battleship.databinding.FragmentGameBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class GameFragment : Fragment() {
    private var _b: FragmentGameBinding? = null
    private val b get() = _b!!
    private val args: GameFragmentArgs by navArgs()
    private val vm: GameViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }
    private var finishDialogShown = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentGameBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm.init(args.gameId, args.isPlayer1)

        b.boardMine.showShips    = true
        b.boardMine.isInteractive = false
        b.boardEnemy.showShips   = false
        b.boardEnemy.cellClickListener = object : BoardView.OnCellClickListener {
            override fun onCellClick(row: Int, col: Int) { vm.fireShot(row, col) }
        }

        vm.myBoard.observe(viewLifecycleOwner)    { b.boardMine.updateBoard(it) }
        vm.enemyBoard.observe(viewLifecycleOwner) { b.boardEnemy.updateBoard(it) }
        vm.statusText.observe(viewLifecycleOwner) { b.tvStatus.text = it }

        vm.isMyTurn.observe(viewLifecycleOwner) { myTurn ->
            b.boardEnemy.isInteractive = myTurn
            b.tvTurnIndicator.text =
                if (myTurn) "🎯  YOUR TURN — Tap to fire!" else "⏳  Opponent's turn…"
            b.tvTurnIndicator.setBackgroundColor(
                requireContext().getColor(if (myTurn) R.color.turn_mine else R.color.turn_opponent)
            )
        }

        vm.waiting.observe(viewLifecycleOwner) { waiting ->
            b.tvWaiting.visibility = if (waiting) View.VISIBLE else View.GONE
        }

        vm.gameFinished.observe(viewLifecycleOwner) { (won, _) ->
            if (won != null && !finishDialogShown) {
                finishDialogShown = true
                showFinishDialog(won)
            }
        }

        vm.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(b.root, msg, Snackbar.LENGTH_LONG).show()
                vm.clearError()
            }
        }
    }

    private fun showFinishDialog(won: Boolean) {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (won) "Victory! 🎉" else "Defeated! 💀")
            .setMessage(
                if (won) "All enemy ships sunk!\nWell played, Admiral!"
                else "Your fleet was destroyed.\nBetter luck next time!"
            )
            .setPositiveButton("Back to Lobby") { _, _ ->
                findNavController().navigate(R.id.action_gameFragment_to_lobbyFragment)
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
