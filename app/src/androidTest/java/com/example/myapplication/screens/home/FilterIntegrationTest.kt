package com.example.myapplication.screens.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.myapplication.HiltTestActivity
import com.example.myapplication.ui.screens.home.FilterScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.TrainerCategory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FilterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun filterScreen_interactions_and_apply() {
        var navigatedBack = false

        composeRule.setContent {
            MyApplicationTheme {
                FilterScreen(
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }

        composeRule.onNodeWithText("Cena za godzinę").assertIsDisplayed()

        composeRule.onNodeWithTag("filter_price_min")
            .performScrollTo()
            .performClick()
            .performTextReplacement("50")

        composeRule.onNodeWithTag("filter_price_max")
            .performScrollTo()
            .performClick()
            .performTextReplacement("200")

        val firstCategory = TrainerCategory.entries.first()

        composeRule.onNodeWithTag("chip_${firstCategory.name}")
            .performScrollTo()
            .performClick()

        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("filter_apply_btn")
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assert(navigatedBack) { "Kliknięcie Zastosuj nie zamknęło ekranu!" }
        }
    }

    @Test
    fun filterScreen_clearButton_resetsValues() {
        composeRule.setContent {
            MyApplicationTheme {
                FilterScreen(onNavigateBack = {})
            }
        }

        composeRule.onNodeWithTag("filter_price_min")
            .performClick()
            .performTextReplacement("999")

        composeRule.onNodeWithTag("filter_price_min")
            .assertTextContains("999")

        composeRule.onNodeWithTag("filter_clear_btn")
            .performClick()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("filter_price_min")
            .assertTextContains("0")
    }
}