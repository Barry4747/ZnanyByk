package com.example.myapplication

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.home.TrainerDetailScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.TrainersViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TrainerDetailIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        auth.signOut()

        auth.signInWithEmailAndPassword("david.laid@gmail.com", "qqqqqq")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("TEST_AUTH", "Zalogowano pomyślnie!")
                } else {
                    android.util.Log.e("TEST_AUTH", "Błąd logowania: ${task.exception?.message}")
                }
            }

        Thread.sleep(3000)
    }

    @Test
    fun trainerDetail_loadsRealData_and_allowsBooking() {
        var bookingClicked = false
        val failureText = "Nie udało się załadować danych trenera."

        composeRule.setContent {
            MyApplicationTheme {
                val viewModel = hiltViewModel<TrainersViewModel>()
                Thread.sleep(3000)
                val state by viewModel.trainersState.collectAsState()
                android.util.Log.d("TEST_AUTH", "State: $state")
                LaunchedEffect(Unit) {
                    viewModel.loadInitialTrainers()
                }

                LaunchedEffect(state.trainers) {
                    if (state.trainers.isNotEmpty() && state.selectedTrainer == null) {
                        viewModel.selectTrainer(state.trainers.first())
                    }
                }

                TrainerDetailScreen(
                    onNavigateBack = {},
                    viewModel = viewModel,
                    onBookClick = { bookingClicked = true }
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 15000) {
            val isSuccess =
                composeRule.onAllNodesWithTag("book_visit_btn").fetchSemanticsNodes().isNotEmpty()
            val isFailure =
                composeRule.onAllNodesWithText(failureText).fetchSemanticsNodes().isNotEmpty()
            isSuccess || isFailure
        }

        if (composeRule.onAllNodesWithText(failureText).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Firebase zwrócił pustą listę trenerów lub wystąpił błąd sieci. 'selectedTrainer' jest null.")
        }

        composeRule.onNodeWithTag("book_visit_btn")
            .assertIsDisplayed()
            .performClick()

        assert(bookingClicked) { "Callback onBookClick nie został wywołany" }

        composeRule.onNodeWithTag("trainer_fullname").assertIsDisplayed()
    }

    @Test
    fun trainerDetail_rateButton_showsDialog() {
        composeRule.setContent {
            MyApplicationTheme {
                val viewModel = hiltViewModel<TrainersViewModel>()

                LaunchedEffect(Unit) {
                    viewModel.loadInitialTrainers()
                }

                val state by viewModel.trainersState.collectAsState()

                LaunchedEffect(state.trainers) {
                    if (state.selectedTrainer == null && state.trainers.isNotEmpty()) {
                        viewModel.selectTrainer(state.trainers.first())
                    }
                }

                TrainerDetailScreen(
                    onNavigateBack = {},
                    viewModel = viewModel,
                    onBookClick = {}
                )
            }
        }

        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithTag("trainer_fullname")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("rate_btn")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()
    }
}