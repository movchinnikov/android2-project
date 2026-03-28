package com.artfinder.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.artfinder.HiltTestActivity
import com.artfinder.ui.theme.ArtFinderTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ArtFinderUiTest {

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
    fun testAuthNavigationFlow() {
        composeTestRule.setContent {
            ArtFinderTheme {
                ArtFinderApp()
            }
        }
        
        // Find "Don't have an account? Register" text and click it
        // We use wait/existence check because it might take a moment to load
        composeTestRule.onNodeWithText("Don't have an account? Register")
            .assertExists()
            .performClick()
            
        // Check if we are on Register screen
        composeTestRule.onNodeWithText("Join ArtFinder").assertIsDisplayed()
        
        // Go back to login
        composeTestRule.onNodeWithText("Already have an account? Login")
            .assertExists()
            .performClick()
            
        // Check if we are back on Login screen
        composeTestRule.onNodeWithText("ArtFinder 🎨").assertIsDisplayed()
    }

    @Test
    fun testInputFieldsExist() {
        composeTestRule.setContent {
            ArtFinderTheme {
                ArtFinderApp()
            }
        }
        
        // Assert email and password fields exist on login
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
    }
}
