package com.example.myapplication.screens.schedule

import com.example.myapplication.HiltTestActivity

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.ui.screens.scheduler.TrainerScheduleScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TrainerScheduleIntegrationTest {

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
            .addOnSuccessListener { Log.d("TEST_DEBUG", "1. Zalogowano: ${it.user?.email}") }
            .addOnFailureListener { Log.e("TEST_DEBUG", "1. BŁĄD LOGOWANIA: ${it.message}") }

        Thread.sleep(4000)
    }

    @Test
    fun addingSlot_updatesFirebase_and_showsOnScreen() {
        val randomMinute = (10..59).random()
        val uniqueTime = "13:$randomMinute"
        val testDuration = 60
        val testDayKey = "monday"

        val expectedUiText = "$uniqueTime ($testDuration min)"


        lateinit var viewModel: ScheduleViewModel

        composeRule.setContent {
            MyApplicationTheme {
                viewModel = hiltViewModel<ScheduleViewModel>()
                TrainerScheduleScreen(viewModel = viewModel)
            }
        }


        try {
            composeRule.waitUntil(timeoutMillis = 10000) {
                composeRule.onAllNodes(hasText("Monday", substring = true) or hasText("Poniedziałek", substring = true))
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Exception) {
            composeRule.onRoot().printToLog("UI_FAIL_LOAD")
            throw e
        }


        composeRule.runOnUiThread {
            viewModel.addNewSlot(testDayKey, TrainingSlot(time = uniqueTime, duration = testDuration))
        }

        Thread.sleep(3000)

        composeRule.runOnUiThread {
            viewModel.loadSchedule()
        }

        Thread.sleep(2000)

        composeRule.runOnIdle {
            val schedule = viewModel.weeklySchedule.value
            val mondaySlots = schedule?.monday ?: emptyList()
            val existsInData = mondaySlots.any { it.time == uniqueTime }

            if (existsInData) {
                Log.d("TEST_DEBUG", "Slot znaleziony w ViewModelu.")
            } else {
                throw AssertionError("Dane nie zapisały się w Firebase lub nie zostały pobrane.")
            }
        }


        try {
            composeRule.waitUntil(timeoutMillis = 10000) {
                val found = composeRule.onAllNodesWithText(expectedUiText).fetchSemanticsNodes().isNotEmpty()
                found
            }
        } catch (e: Exception) {
            composeRule.onRoot().printToLog("UI_FAIL_SLOT")

            try {
                composeRule.onNodeWithText(expectedUiText).performScrollTo()
            } catch (scrollE: Exception) {
                Log.e("TEST_DEBUG", "Scrollowanie też nie pomogło.")
            }
            throw e
        }

        composeRule.onNodeWithText(expectedUiText).assertIsDisplayed()
        Log.d("TEST_DEBUG", "9. SUKCES! Test zakończony.")

        composeRule.runOnUiThread {
            viewModel.removeSlot(testDayKey, TrainingSlot(time = uniqueTime, duration = testDuration))
        }
    }
}