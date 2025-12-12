package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun homeScreen_guestUser_showsContent() {
        FirebaseAuth.getInstance().signOut()

        composeRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    onMapClick = {},
                    goToFilter = {},
                    goToTrainerProfileCard = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("No user logged in").assertDoesNotExist()

        composeRule.onNodeWithTag("search_field").assertIsDisplayed()
        composeRule.onNodeWithTag("filter_btn").assertIsDisplayed()
    }

    @Test
    fun homeScreen_loggedIn_showsContent() {
        val email = "david.laid@gmail.com"
        val password = "qqqqqq"

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
        }

        Thread.sleep(2000)

        composeRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    onMapClick = {},
                    goToFilter = {},
                    goToTrainerProfileCard = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("No user logged in").assertDoesNotExist()

        composeRule.onNodeWithTag("search_field").assertIsDisplayed()
        composeRule.onNodeWithTag("filter_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("map_btn").assertIsDisplayed()

        composeRule.onNodeWithTag("search_field").performTextInput("Testowy Trener")

    }
}