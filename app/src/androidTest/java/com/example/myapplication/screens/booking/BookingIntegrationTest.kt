package com.example.myapplication.screens.booking

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.HiltTestActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.booking.BookingViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@HiltAndroidTest
class BookingIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword("david.laid@gmail.com", "qqqqqq")
        Thread.sleep(3000)
    }

    @Test
    fun bookingViewModel_integration_fetchesSlots() = runBlocking {
        lateinit var viewModel: BookingViewModel

        composeRule.setContent {
            MyApplicationTheme {
                viewModel = hiltViewModel<BookingViewModel>()
            }
        }

        val trainerId = "mETmdrUlgtcDZIdfaCyG42UyeXv1"

        composeRule.runOnUiThread {
            viewModel.init(trainerId)
        }

        composeRule.waitUntil(timeoutMillis = 10000) {
            !viewModel.uiState.value.isLoading
        }

        val tomorrow = LocalDate.now().plusDays(1)
        composeRule.runOnUiThread {
            viewModel.onDateSelected(tomorrow)
        }

        composeRule.waitForIdle()

        val state = viewModel.uiState.value

        assert(state.trainerId == trainerId) { "TrainerID nie zostało ustawione" }

        assert(state.selectedDate == tomorrow) { "Data nie została zmieniona" }

    }
}