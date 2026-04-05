package com.example.battleship.lobby

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.battleship.App
import com.example.battleship.R
import com.example.battleship.common.*
import com.example.battleship.databinding.FragmentLobbyBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class LobbyFragment : Fragment() {
    private var _b: FragmentLobbyBinding? = null
    private val b get() = _b!!

    private val vm: LobbyViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentLobbyBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        b.btnCreateGame.setOnClickListener { vm.createGame() }
        b.btnJoinGame.setOnClickListener   { showJoinDialog() }

        vm.nickname.observe(viewLifecycleOwner) {
            b.tvWelcome.text = "Welcome, $it! ⚓"
        }

        vm.createResult.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Result.Loading -> { b.progressBar.visible(); b.btnCreateGame.isEnabled = false }
                is Result.Success -> {
                    b.progressBar.gone(); b.btnCreateGame.isEnabled = true
                    showGameIdDialog(res.data)
                }
                is Result.Error -> {
                    b.progressBar.gone(); b.btnCreateGame.isEnabled = true
                    snack(res.message)
                }
            }
        }

        vm.joinResult.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Result.Loading -> { b.progressBar.visible(); b.btnJoinGame.isEnabled = false }
                is Result.Success -> {
                    b.progressBar.gone(); b.btnJoinGame.isEnabled = true
                    val action = LobbyFragmentDirections
                        .actionLobbyFragmentToPlacementFragment(res.data.id, false)
                    findNavController().navigate(action)
                }
                is Result.Error -> {
                    b.progressBar.gone(); b.btnJoinGame.isEnabled = true
                    snack(res.message)
                }
            }
        }
    }

    private fun showJoinDialog() {
        val px16 = (16 * resources.displayMetrics.density).toInt()
        val et = EditText(requireContext()).apply {
            hint = "6-character game ID"
            filters = arrayOf(
                android.text.InputFilter.AllCaps(),
                android.text.InputFilter.LengthFilter(6)
            )
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px16 + 8, 0, px16 + 8, 0)
            addView(et)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Join Game")
            .setMessage("Enter the 6-character game ID shared by your opponent.")
            .setView(container)
            .setPositiveButton("Join") { _, _ ->
                val id = et.text?.toString()?.trim()?.uppercase() ?: ""
                if (id.length == 6) vm.joinGame(id)
                else snack("Game ID must be exactly 6 characters")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGameIdDialog(gameId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Game Created! 🎮")
            .setMessage("Share this code with your opponent:\n\n" +
                    "  $gameId  \n\n" +
                    "Waiting for them to join before you can place ships.")
            .setPositiveButton("Proceed to placement") { _, _ ->
                val action = LobbyFragmentDirections
                    .actionLobbyFragmentToPlacementFragment(gameId, true)
                findNavController().navigate(action)
            }
            .setCancelable(false)
            .show()
    }

    private fun snack(msg: String) =
        Snackbar.make(b.root, msg, Snackbar.LENGTH_LONG).show()

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
