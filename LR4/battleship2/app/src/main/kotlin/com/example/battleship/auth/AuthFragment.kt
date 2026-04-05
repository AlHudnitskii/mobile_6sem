package com.example.battleship.auth

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.battleship.App
import com.example.battleship.R
import com.example.battleship.common.*
import com.example.battleship.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {
    private var _b: FragmentAuthBinding? = null
    private val b get() = _b!!
    private var isLogin = true
    private val vm: AuthViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAuthBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        updateMode()
        b.btnToggleMode.setOnClickListener { isLogin = !isLogin; updateMode() }
        b.btnSubmit.setOnClickListener { submit() }
        vm.result.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Result.Loading -> { b.progressBar.visible(); b.btnSubmit.isEnabled = false }
                is Result.Success -> {
                    b.progressBar.gone(); b.btnSubmit.isEnabled = true
                    findNavController().navigate(R.id.action_authFragment_to_lobbyFragment)
                }
                is Result.Error -> {
                    b.progressBar.gone(); b.btnSubmit.isEnabled = true
                    b.root.snack(res.message)
                }
            }
        }
    }

    private fun updateMode() {
        if (isLogin) {
            b.tvTitle.text = getString(R.string.sign_in)
            b.tilNickname.gone()
            b.btnSubmit.text = getString(R.string.sign_in)
            b.btnToggleMode.text = getString(R.string.create_account)
        } else {
            b.tvTitle.text = getString(R.string.create_account)
            b.tilNickname.visible()
            b.btnSubmit.text = getString(R.string.create_account)
            b.btnToggleMode.text = getString(R.string.sign_in)
        }
    }

    private fun submit() {
        val email = b.etEmail.text?.toString()?.trim() ?: ""
        val pass  = b.etPassword.text?.toString() ?: ""
        if (email.isBlank() || pass.isBlank()) { b.root.snack(getString(R.string.fill_all_fields)); return }
        if (isLogin) vm.signIn(email, pass)
        else {
            val nick = b.etNickname.text?.toString()?.trim() ?: ""
            if (nick.isBlank()) { b.root.snack(getString(R.string.fill_all_fields)); return }
            vm.signUp(email, pass, nick)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
