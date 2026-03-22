package com.artfinder.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artfinder.data.repository.AuthRepository
import com.artfinder.data.repository.UserRepository
import com.artfinder.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, psw: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email, psw)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    fun register(email: String, psw: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.register(email, psw)
                if (user != null) {
                    userRepository.saveUserProfile(user.uid, UserProfile(email = email, name = name))
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
