package com.artfinder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.artfinder.ui.art.ArtDetailScreen
import com.artfinder.ui.art.ArtListScreen
import com.artfinder.ui.auth.LoginScreen
import com.artfinder.ui.auth.RegisterScreen
import com.artfinder.ui.map.MapScreen
import com.artfinder.ui.profile.ProfileScreen
import com.artfinder.ui.profile.SettingsScreen
import com.artfinder.ui.profile.EditNameScreen
import com.artfinder.ui.profile.ChangePasswordScreen
import com.artfinder.ui.theme.ArtFinderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtFinderApp()
        }
    }
}

@Composable
fun ArtFinderApp() {
    val navController = rememberNavController()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val startDest = if (auth.currentUser != null) "art_list" else "login"
    
    var currentTheme by rememberSaveable { mutableStateOf("System") }
    val isDark = when (currentTheme) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

    ArtFinderTheme(darkTheme = isDark) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute != null && currentRoute != "login" && currentRoute != "register" && currentRoute != "settings" && !currentRoute.startsWith("art_detail")) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Art") },
                            label = { Text("Art") },
                            selected = currentRoute == "art_list",
                            onClick = { 
                                navController.navigate("art_list") {
                                    popUpTo("art_list") { inclusive = true }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                            label = { Text("Map") },
                            selected = currentRoute == "map",
                            onClick = { navController.navigate("map") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentRoute == "profile",
                            onClick = { navController.navigate("profile") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate("register") },
                        onLoginSuccess = { navController.navigate("art_list") { popUpTo("login") { inclusive = true } } }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onNavigateToLogin = { navController.navigate("login") },
                        onRegisterSuccess = { navController.navigate("art_list") { popUpTo("register") { inclusive = true } } }
                    )
                }
                composable("art_list") {
                    ArtListScreen(onNavigateToDetails = { id -> navController.navigate("art_detail/$id") })
                }
                composable("map") {
                    Text("Map Screen")
                }
                composable("profile") {
                    ProfileScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToEditName = { navController.navigate("edit_name") },
                        onNavigateToChangePassword = { navController.navigate("change_password") },
                        onLogout = { navController.navigate("login") { popUpTo(0) } }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        currentTheme = currentTheme,
                        onThemeChange = { currentTheme = it }
                    )
                }
                composable("edit_name") {
                    EditNameScreen(onBack = { navController.popBackStack() })
                }
                composable("change_password") {
                    ChangePasswordScreen(onBack = { navController.popBackStack() })
                }
            composable(
                "art_detail/{artId}",
                arguments = listOf(navArgument("artId") { type = NavType.IntType })
            ) { backStackEntry ->
                val artId = backStackEntry.arguments?.getInt("artId") ?: 0
                ArtDetailScreen(
                    artId = artId,
                    onBack = { navController.popBackStack() }
                )
                }
            }
        }
    }
}
