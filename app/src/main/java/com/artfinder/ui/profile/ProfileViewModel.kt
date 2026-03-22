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

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    init {
        loadProfile()
        loadLeaderboard()
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

    fun loadLeaderboard() {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardState.Loading
            try {
                val allUsers = userRepository.getAllUsers().sortedByDescending { it.points }
                val currentUid = userRepository.currentUser?.uid
                
                val rankedUsers = mutableListOf<RankedUser>()
                var i = 0
                while (i < allUsers.size) {
                    val points = allUsers[i].points
                    val group = allUsers.filter { it.points == points }
                    val groupSize = group.size
                    val startRank = i + 1
                    val endRank = i + groupSize
                    
                    val rankDisplay = if (groupSize > 1) {
                        "$startRank-$endRank"
                    } else {
                        "$startRank"
                    }
                    
                    group.forEach { user ->
                        rankedUsers.add(RankedUser(user, rankDisplay, user.id == currentUid))
                    }
                    i += groupSize
                }
                
                val top20 = rankedUsers.take(20)
                val currentUserRank = rankedUsers.find { it.isCurrentUser }
                
                _leaderboardState.value = LeaderboardState.Success(
                    top20 = top20,
                    currentUserRank = if (top20.none { it.isCurrentUser }) currentUserRank else null
                )
            } catch (e: Exception) {
                _leaderboardState.value = LeaderboardState.Error(e.message ?: "Failed to load leaderboard")
            }
        }
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

data class RankedUser(
    val user: com.artfinder.data.model.UserProfile,
    val rankDisplay: String,
    val isCurrentUser: Boolean
)

sealed class LeaderboardState {
    object Loading : LeaderboardState()
    data class Success(val top20: List<RankedUser>, val currentUserRank: RankedUser?) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}
