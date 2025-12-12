package com.example.myapplication.screens.booking

import com.example.myapplication.HiltTestActivity


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.booking.PaymentUiState
import com.example.myapplication.viewmodel.booking.PaymentViewModel
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@HiltAndroidTest
class PaymentIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        auth.signInWithEmailAndPassword("david.laid@gmail.com", "qqqqqq")
        Thread.sleep(3000)
    }

    @Test
    fun paymentProcess_savesAppointment_toRealDatabase()  {
        lateinit var paymentViewModel: PaymentViewModel
        lateinit var scheduleViewModel: ScheduleViewModel

        composeRule.setContent {
            MyApplicationTheme {
                paymentViewModel = hiltViewModel<PaymentViewModel>()
                scheduleViewModel = hiltViewModel<ScheduleViewModel>()
            }
        }

        val uniqueTitle = "Test BOOKING ${UUID.randomUUID().toString().take(8)}"
        val trainerId = "test-trainer"
        val nowMillis = System.currentTimeMillis()
        val time = "14:00"

        composeRule.runOnUiThread {
            paymentViewModel.processPayment(
                trainerId = trainerId,
                dateMillis = nowMillis,
                time = time,
                title = uniqueTitle
            )
        }

        composeRule.waitUntil(timeoutMillis = 10000) {
            paymentViewModel.uiState.value is PaymentUiState.Success
        }

        val finalState = paymentViewModel.uiState.value
        if (finalState is PaymentUiState.Error) {
            throw AssertionError("Błąd płatności: ${finalState.message}")
        }

        composeRule.runOnUiThread {
            scheduleViewModel.appointments.observeForever { }
            scheduleViewModel.loadAppointments()
        }

        composeRule.waitUntil(timeoutMillis = 10000) {
            val list = scheduleViewModel.appointments.value
            list?.any { it.title == uniqueTitle } == true
        }

        val fetchedAppointments = scheduleViewModel.appointments.value ?: emptyList()
        val myAppointment = fetchedAppointments.find { it.title == uniqueTitle }

        assert(myAppointment != null) { "Wizyta nie została znaleziona w bazie!" }
        assert(myAppointment?.trainerId == trainerId) { "Złe ID trenera w bazie" }
        assert(myAppointment?.time == time) { "Zła godzina w bazie" }

        android.util.Log.d("TEST_BOOKING", "Sukces! Znaleziono wizytę w bazie: $myAppointment")
    }
}