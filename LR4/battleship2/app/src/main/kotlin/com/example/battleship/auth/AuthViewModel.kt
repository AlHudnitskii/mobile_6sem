package com.example.battleship.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.battleship.common.Result
import com.example.battleship.common.SupabaseRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: SupabaseRepository) : ViewModel() {
    private val _result = MutableLiveData<Result<Unit>>()
    val result: LiveData<Result<Unit>> = _result

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _result.value = Result.Loading
        _result.value = repo.signIn(email, password)
    }

    fun signUp(email: String, password: String, nickname: String) = viewModelScope.launch {
        _result.value = Result.Loading
        _result.value = repo.signUp(email, password, nickname)
    }
}
