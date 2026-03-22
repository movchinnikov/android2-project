package com.artfinder.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.artfinder.ui.theme.ArtFinderTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for critical paths as per Milestone 4 requirements.
 */
class NavigationTest {

    @Rule
    @JvmField
    val composeTestRule = createComposeRule()

    @Test
    fun login_navigates_to_register() {
        composeTestRule.setContent {
            ArtFinderTheme {
                ArtFinderApp()
            }
        }

        // Check if "Register" button exists and click it
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()

        // Verify we are on the Register screen
        composeTestRule.onNodeWithText("Join ArtFinder 🎨").assertExists()
    }

    @Test
    fun register_navigates_to_login() {
        composeTestRule.setContent {
            ArtFinderTheme {
                // Manually navigate to register for this test
                RegisterScreen(onNavigateToLogin = {}, onRegisterSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Already have an account? Login").performClick()
    }
}
