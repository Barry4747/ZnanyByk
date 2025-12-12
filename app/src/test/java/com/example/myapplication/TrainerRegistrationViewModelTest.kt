package com.example.myapplication

import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.users.Role
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.trainer.TrainerRegistrationViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TrainerRegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val trainerRepository: TrainerRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val gymRepository: GymRepository = mockk(relaxed = true)

    private lateinit var viewModel: TrainerRegistrationViewModel

    private val testUser = User(
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
        role = Role.CLIENT
    )

    private val testGyms = listOf(
        Gym(id = "1", gymName = "Gym A"),
        Gym(id = "2", gymName = "Gym B")
    )

    @Before
    fun setup() {
        coEvery { gymRepository.getAllGyms() } returns Result.success(testGyms)
        every { authRepository.getCachedUser() } returns Pair("token", testUser)
        every { authRepository.getCurrentUserEmail() } returns "john@example.com"
        every { authRepository.getCurrentUserId() } returns "testId"
    }

    private fun createViewModel() {
        viewModel = TrainerRegistrationViewModel(
            authRepository,
            trainerRepository,
            userRepository,
            gymRepository
        )
    }


    @Test
    fun `submitTrainerProfile fails when hourly rate is invalid`() = runTest {
        createViewModel()

        viewModel.submitTrainerProfile(
            hourlyRate = "0",
            gymId = null,
            description = "Desc",
            experienceYears = "5",
            selectedCategories = emptyList(),
            images = emptyList()
        )

        val state = viewModel.state.value
        assertEquals("Kwota za godzinę musi być liczbą większą od 0", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `submitTrainerProfile fails when experience is invalid`() = runTest {
        createViewModel()

        viewModel.submitTrainerProfile(
            hourlyRate = "100",
            gymId = null,
            description = "Desc",
            experienceYears = "-1",
            selectedCategories = emptyList(),
            images = emptyList()
        )

        val state = viewModel.state.value
        assertEquals("Doświadczenie musi być liczbą większą lub równą 0", state.errorMessage)
    }
    @Test
    fun `submitTrainerProfile fails when user cache is missing`() = runTest {
        every { authRepository.getCachedUser() } returns null
        createViewModel()

        viewModel.submitTrainerProfile(
            hourlyRate = "100",
            gymId = null,
            description = "Desc",
            experienceYears = "5",
            selectedCategories = emptyList(),
            images = emptyList()
        )

        assertEquals("Nie znaleziono danych użytkownika", viewModel.state.value.errorMessage)
    }


    @Test
    fun `submitTrainerProfile success`() = runTest {
        createViewModel()

        coEvery { trainerRepository.addTrainer(any(), any()) } returns Result.success("newTrainerId")
        coEvery { userRepository.updateUserRole(any(), any()) } returns Result.success(Unit)

        viewModel.submitTrainerProfile(
            hourlyRate = "100",
            gymId = "gym1",
            description = "I am a trainer",
            experienceYears = "5",
            selectedCategories = listOf("Yoga"),
            images = listOf("url1")
        )

        advanceUntilIdle()

        val state = viewModel.state.value

        coVerify { trainerRepository.addTrainer(match { it.pricePerHour == 100 && it.experience == 5 }, "testId") }

        coVerify { userRepository.updateUserRole("testId", Role.TRAINER) }

        verify { authRepository.saveCachedUser(match { it.role == Role.TRAINER }, "testId") }

        assertEquals("Profil trenera został utworzony pomyślnie!", state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `submitTrainerProfile handles repository failure`() = runTest {
        createViewModel()

        val exception = Exception("Database error")
        coEvery { trainerRepository.addTrainer(any(), any()) } returns Result.failure(exception)

        viewModel.submitTrainerProfile(
            hourlyRate = "100",
            gymId = null,
            description = "Desc",
            experienceYears = "5",
            selectedCategories = emptyList(),
            images = emptyList()
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Błąd podczas tworzenia profilu: Database error", state.errorMessage)
        assertFalse(state.isLoading)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}