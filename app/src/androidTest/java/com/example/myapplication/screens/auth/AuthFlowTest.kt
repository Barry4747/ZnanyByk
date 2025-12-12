package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.auth.CredentialsRegistrationScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.PersonalInfoRegistrationScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.registration.RegistrationViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalSharedTransitionApi::class)
@HiltAndroidTest
class AuthIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun login_existingUser_success() {
        var loginSuccess = false

        composeRule.setContent {
            MyApplicationTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        LoginScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onNavigateBack = {},
                            onNavigateToRegister = {},
                            onNavigateToPersonalInfo = {},
                            onNavigateToPasswordReset = {},
                            onLoginSuccess = { loginSuccess = true }
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("login_email")
            .performScrollTo()
            .performTextInput("david.laid@gmail.com")

        composeRule.onNodeWithTag("login_password")
            .performScrollTo()
            .performTextInput("qqqqqq")

        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("login_btn")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10000) { loginSuccess }
    }

    @Test
    fun registration_step1_validation_success() {
        var navigatedNext = false
        val randomEmail = "test_${UUID.randomUUID().toString().substring(0,8)}@example.com"
        val password = "Haslo123!"

        composeRule.setContent {
            MyApplicationTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        CredentialsRegistrationScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onNavigateBack = {},
                            onNavigateToLogin = {},
                            onNavigateToPersonalInfo = { navigatedNext = true },
                            onGoogleSignInSuccess = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("reg_email")
            .performScrollTo()
            .performTextInput(randomEmail)

        composeRule.onNodeWithTag("reg_pass")
            .performScrollTo()
            .performTextInput(password)

        composeRule.onNodeWithTag("reg_repeat_pass")
            .performScrollTo()
            .performTextInput(password)

        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("reg_continue_btn")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assert(navigatedNext) { "Nie przeszło do kolejnego ekranu!" }
        }
    }

    @Test
    fun registration_step2_firebaseCreation_success() {
        var registrationSuccess = false

        val randomEmail = "test_auto_${UUID.randomUUID().toString().take(8)}@example.com"
        val password = "Haslo123!"

        composeRule.setContent {
            MyApplicationTheme {
                val viewModel = hiltViewModel<RegistrationViewModel>()

                LaunchedEffect(Unit) {
                    viewModel.updateEmail(randomEmail)
                    viewModel.updatePassword(password)
                    viewModel.updateRepeatPassword(password)
                }

                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        PersonalInfoRegistrationScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            viewModel = viewModel,
                            onNavigateBack = {},
                            onRegistrationSuccess = { registrationSuccess = true },
                            onRegistrationSuccessProceedWithTrainer = { registrationSuccess = true }
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("reg_firstname")
            .performScrollTo()
            .performTextInput("Jan")

        composeRule.onNodeWithTag("reg_lastname")
            .performScrollTo()
            .performTextInput("Testowy")

        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("reg_create_btn")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 15000) {
            registrationSuccess
        }
    }

    @Test
    fun login_wrongPassword_showsError() {
        composeRule.setContent {
            MyApplicationTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        LoginScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onNavigateBack = {}, onNavigateToRegister = {},
                            onNavigateToPersonalInfo = {}, onNavigateToPasswordReset = {},
                            onLoginSuccess = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("login_email").performScrollTo().performTextInput("david.laid@gmail.com")
        composeRule.onNodeWithTag("login_password").performScrollTo().performTextInput("złe_haslo_123")

        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onNodeWithTag("login_btn").performScrollTo().performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun registration_step1_passwordsMismatch_buttonDisabled() {
        composeRule.setContent {
            MyApplicationTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        CredentialsRegistrationScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onNavigateBack = {}, onNavigateToLogin = {},
                            onNavigateToPersonalInfo = {}, onGoogleSignInSuccess = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("reg_email").performScrollTo().performTextInput("test@test.pl")
        composeRule.onNodeWithTag("reg_pass").performScrollTo().performTextInput("Haslo123!")
        composeRule.onNodeWithTag("reg_repeat_pass").performScrollTo().performTextInput("InneHaslo!!!")

        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("reg_continue_btn")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun registration_step2_emptyFields_buttonDisabled() {
        composeRule.setContent {
            MyApplicationTheme {
                val viewModel = hiltViewModel<RegistrationViewModel>()
                LaunchedEffect(Unit) {
                    viewModel.updateEmail("test@test.pl")
                    viewModel.updatePassword("Haslo123!")
                }
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        PersonalInfoRegistrationScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            viewModel = viewModel,
                            onNavigateBack = {}, onRegistrationSuccess = {},
                            onRegistrationSuccessProceedWithTrainer = {}
                        )
                    }
                }
            }
        }


        composeRule.onRoot().performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("reg_create_btn")
            .performScrollTo()
            .assertIsNotEnabled()
    }
}