package com.artfinder.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artfinder.domain.gamification.Badge

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToEditName: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (val state = profileState) {
            is ProfileState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is ProfileState.Success -> {
                val profile = state.profile
                Text("User Profile", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(profile.name, style = MaterialTheme.typography.titleLarge)
                Text(profile.email, style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row {
                    Button(onClick = onNavigateToEditName) {
                        Text("Edit Name")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onNavigateToChangePassword) {
                        Text("Change Password")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Points: ${profile.points}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Badge: ${profile.badge} ${getBadgeEmoji(profile.badge)}", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToLeaderboard,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Global Leaderboard")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Settings")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            }
            is ProfileState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun getBadgeEmoji(badgeName: String): String {
    return Badge.values().find { it.displayName == badgeName }?.icon ?: "🧭"
}
