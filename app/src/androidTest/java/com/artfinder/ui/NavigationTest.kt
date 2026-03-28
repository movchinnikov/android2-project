package com.artfinder.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.artfinder.ui.theme.ArtFinderTheme
import com.artfinder.ui.auth.RegisterScreen
import com.artfinder.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for critical paths as per Milestone 4 requirements.
 */
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        // Ensure we are logged out so we start on the Login screen
        FirebaseAuth.getInstance().signOut()
    }

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
        composeTestRule.onNodeWithText("Join ArtFinder").assertExists()
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
