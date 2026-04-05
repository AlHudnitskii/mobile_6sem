package com.example.battleship.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.battleship.common.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: SupabaseRepository) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            when (val r = repo.getProfile()) {
                is Result.Success -> _profile.value = r.data
                is Result.Error   -> _message.value = r.message
                else -> {}
            }
            _loading.value = false
        }
    }

    fun updateNickname(nick: String) {
        viewModelScope.launch {
            _loading.value = true
            when (val r = repo.updateNickname(nick)) {
                is Result.Success -> {
                    _profile.value = _profile.value?.copy(nickname = nick)
                    _message.value = "Nickname updated!"
                }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _loading.value = false
        }
    }

    fun selectPreset(index: Int) {
        viewModelScope.launch {
            _loading.value = true
            when (val r = repo.updateAvatarIndex(index)) {
                is Result.Success -> {
                    _profile.value = _profile.value?.copy(avatarIndex = index, avatarUrl = "")
                    _message.value = "Avatar updated!"
                }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _loading.value = false
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            _message.value = "Uploading…"
            when (val r = repo.uploadAvatar(context, uri)) {
                is Result.Success -> {
                    _profile.value = _profile.value?.copy(avatarUrl = r.data, avatarIndex = -1)
                    _message.value = "Avatar uploaded!"
                }
                is Result.Error -> _message.value = "Upload failed: ${r.message}"
                else -> {}
            }
            _loading.value = false
        }
    }

    fun signOut() = repo.signOut()
    fun clearMessage() { _message.value = null }
}
