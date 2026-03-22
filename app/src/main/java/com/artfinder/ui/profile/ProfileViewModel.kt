package com.artfinder.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artfinder.data.model.UserProfile
import com.artfinder.data.repository.AuthRepository
import com.artfinder.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _editStatus = MutableStateFlow<EditStatus>(EditStatus.Idle)
    val editStatus: StateFlow<EditStatus> = _editStatus

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val profile = userRepository.getUserProfile()
                if (profile != null) {
                    _profileState.value = ProfileState.Success(profile)
                } else {
                    _profileState.value = ProfileState.Error("Profile not found")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            _editStatus.value = EditStatus.Loading
            try {
                userRepository.updateProfileName(newName)
                _editStatus.value = EditStatus.Success
                loadProfile()
            } catch (e: Exception) {
                _editStatus.value = EditStatus.Error(e.message ?: "Update failed")
            }
        }
    }

    fun changePassword(oldPsw: String, newPsw: String) {
        viewModelScope.launch {
            _editStatus.value = EditStatus.Loading
            try {
                // First re-authenticate to allow password change
                authRepository.reauthenticate(oldPsw)
                // Then update password
                authRepository.updatePassword(newPsw)
                _editStatus.value = EditStatus.Success
            } catch (e: Exception) {
                _editStatus.value = EditStatus.Error(e.message ?: "Update failed. Check your current password.")
            }
        }
    }

    fun clearStatus() {
        _editStatus.value = EditStatus.Idle
    }

    fun logout() {
        authRepository.logout()
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class EditStatus {
    object Idle : EditStatus()
    object Loading : EditStatus()
    object Success : EditStatus()
    data class Error(val message: String) : EditStatus()
}
