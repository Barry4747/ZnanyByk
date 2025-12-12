package com.example.myapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.registration.RegistrationViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RegistrationViewModelTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var viewModel: RegistrationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = RegistrationViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            webClientId = "fake_client_id",
            appContext = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `updateEmail with invalid format sets error`() {
        val invalidEmail = "matigmail.com"

        viewModel.updateEmail(invalidEmail)

        val state = viewModel.registrationState.value
        assertEquals(context.getString(R.string.invalid_email_format), state.emailValidationError)
        assertEquals(invalidEmail, state.registrationCredentials.email)
    }

    @Test
    fun `updatePassword with too short password sets error`() {
        val shortPass = "12345"

        viewModel.updatePassword(shortPass)

        val state = viewModel.registrationState.value
        assertEquals(context.getString(R.string.minum_characters_password), state.passwordValidationError)
    }

    @Test
    fun `updateRepeatPassword mismatch sets error`() {
        viewModel.updatePassword("haslo123")

        viewModel.updateRepeatPassword("hasloInne")

        val state = viewModel.registrationState.value
        assertEquals(context.getString(R.string.passwords_mismatch), state.passwordValidationError)
    }

    @Test
    fun `updateRepeatPassword matching clears error`() {
        viewModel.updatePassword("haslo123")
        viewModel.updateRepeatPassword("zle")

        viewModel.updateRepeatPassword("haslo123")

        assertNull(viewModel.registrationState.value.passwordValidationError)
    }

    @Test
    fun `updatePhoneNumber invalid length sets error`() {
        val tooShort = "123"
        val countryCode = "+48"

        viewModel.updatePhoneNumber(tooShort, countryCode)

        val state = viewModel.registrationState.value
        assertEquals(context.getString(R.string.invalid_phone_number_length), state.phoneNumberValidationError)
    }



    @Test
    fun `isFormValid returns TRUE when all data is correct`() {
        viewModel.updateEmail("poprawny@email.com")
        viewModel.updatePassword("haslo123")
        viewModel.updateRepeatPassword("haslo123")

        assertNull(viewModel.registrationState.value.passwordValidationError)
        assertNull(viewModel.registrationState.value.emailValidationError)

        val isValid = viewModel.isFormValid()

        assertTrue("Formularz powinien być poprawny", isValid)
    }

    @Test
    fun `isFormValid returns FALSE when passwords do not match`() {
        viewModel.updateEmail("poprawny@email.com")
        viewModel.updatePassword("haslo123")
        viewModel.updateRepeatPassword("inneHaslo")

        val isValid = viewModel.isFormValid()

        assertFalse("Formularz nie powinien przejść przy różnych hasłach", isValid)
    }

    @Test
    fun `isFormValid returns FALSE when email is empty`() {
        viewModel.updateEmail("")
        viewModel.updatePassword("haslo123")
        viewModel.updateRepeatPassword("haslo123")

        val isValid = viewModel.isFormValid()

        assertFalse("Formularz nie powinien przejść bez emaila", isValid)
    }

    @Test
    fun `isFormValid returns FALSE when password is too short`() {
        viewModel.updateEmail("poprawny@email.com")
        viewModel.updatePassword("123")
        viewModel.updateRepeatPassword("123")

        val isValid = viewModel.isFormValid()

        assertFalse("Formularz nie powinien przejść ze słabym hasłem", isValid)
    }
}