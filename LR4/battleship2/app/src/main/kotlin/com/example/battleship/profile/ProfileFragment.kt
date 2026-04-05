package com.example.battleship.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.battleship.App
import com.example.battleship.R
import com.example.battleship.common.*
import com.example.battleship.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!

    private val vm: ProfileViewModel by viewModels {
        RepoViewModelFactory((requireActivity().application as App).repository)
    }

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery() else showPermDenied()
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> vm.uploadAvatar(requireContext(), uri) }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentProfileBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vm.load()

        b.btnSaveNickname.setOnClickListener {
            val nick = b.etNickname.text?.toString()?.trim() ?: ""
            if (nick.isBlank()) snack(getString(R.string.fill_all_fields))
            else vm.updateNickname(nick)
        }

        // Avatar preset selection — clear visual feedback
        b.ivAvatar1.setOnClickListener {
            b.ivAvatar1.alpha = 1f; b.ivAvatar2.alpha = 0.5f
            vm.selectPreset(0)
        }
        b.ivAvatar2.setOnClickListener {
            b.ivAvatar2.alpha = 1f; b.ivAvatar1.alpha = 0.5f
            vm.selectPreset(1)
        }

        b.btnUploadAvatar.setOnClickListener { checkPermAndGallery() }

        b.btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_confirm)
                .setPositiveButton(R.string.yes) { _, _ ->
                    vm.signOut()
                    findNavController().navigate(R.id.action_profileFragment_to_authFragment)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        vm.profile.observe(viewLifecycleOwner) { p ->
            b.etNickname.setText(p.nickname)
            b.tvEmail.text = p.email
            b.tvStats.text = getString(R.string.stats_format, p.totalGames, p.wins, p.losses)

            // Show avatar — url takes priority over preset index
            when {
                p.avatarUrl.isNotBlank() -> {
                    Glide.with(this)
                        .load(p.avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_default)
                        .error(R.drawable.ic_avatar_default)
                        .into(b.ivCurrentAvatar)
                }
                p.avatarIndex == 1 -> {
                    b.ivCurrentAvatar.setImageResource(R.drawable.avatar_2)
                    b.ivAvatar2.alpha = 1f; b.ivAvatar1.alpha = 0.5f
                }
                else -> {
                    b.ivCurrentAvatar.setImageResource(R.drawable.avatar_1)
                    b.ivAvatar1.alpha = 1f; b.ivAvatar2.alpha = 0.5f
                }
            }
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            b.progressBar.isVisible = loading
            b.btnSaveNickname.isEnabled = !loading
            b.btnUploadAvatar.isEnabled = !loading
        }

        vm.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) { snack(msg); vm.clearMessage() }
        }
    }

    private fun checkPermAndGallery() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED ->
                openGallery()
            shouldShowRequestPermissionRationale(perm) ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.gallery_permission_rationale)
                    .setPositiveButton(R.string.grant) { _, _ -> permLauncher.launch(perm) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            else -> permLauncher.launch(perm)
        }
    }

    private fun openGallery() =
        galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))

    private fun showPermDenied() =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_denied)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.ok, null)
            .show()

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
