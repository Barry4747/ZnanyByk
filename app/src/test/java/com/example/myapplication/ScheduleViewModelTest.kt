package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.trainer.BulkScheduleConfig
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import  org.junit.Rule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK private lateinit var scheduleRepository: ScheduleRepository
    @MockK private lateinit var authRepository: AuthRepository
    @MockK private lateinit var userRepository: UserRepository
    @MockK private lateinit var gymRepository: GymRepository

    private val weeklyScheduleLiveData = MutableLiveData<WeeklySchedule>()
    private val appointmentsLiveData = MutableLiveData<List<Appointment>>()

    @MockK(relaxed = true) private lateinit var scheduleObserver: Observer<WeeklySchedule>

    private lateinit var viewModel: ScheduleViewModel

    private val testUserId = "user_123"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { authRepository.getCurrentUserId() } returns testUserId
        every { scheduleRepository.weeklySchedule } returns weeklyScheduleLiveData
        every { scheduleRepository.appointments } returns appointmentsLiveData

        coEvery { scheduleRepository.setWeeklySchedule(any(), any()) } returns Unit
        coEvery { scheduleRepository.getWeeklySchedule(any()) } returns Unit

        viewModel = ScheduleViewModel(scheduleRepository, authRepository, userRepository, gymRepository)

        viewModel.weeklySchedule.observeForever(scheduleObserver)
    }

    @After
    fun tearDown() {
        viewModel.weeklySchedule.removeObserver(scheduleObserver)
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `addNewSlot adds slot to correct day and sorts chronologically`() = runTest {
        val initialSlot = TrainingSlot(time = "14:00", duration = 60)
        val initialSchedule = WeeklySchedule(monday = listOf(initialSlot))

        weeklyScheduleLiveData.value = initialSchedule

        val newSlot = TrainingSlot(time = "09:00", duration = 60)

        viewModel.addNewSlot("Monday", newSlot)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<WeeklySchedule>()
        coVerify { scheduleRepository.setWeeklySchedule(testUserId, capture(slot)) }

        val updatedMonday = slot.captured.monday!!
        assertEquals(2, updatedMonday.size)
        assertEquals("09:00", updatedMonday[0].time)
        assertEquals("14:00", updatedMonday[1].time)
    }

    @Test
    fun `removeSlot removes the correct slot from the correct day`() = runTest {
        val slotToKeep = TrainingSlot(time = "10:00", duration = 60)
        val slotToRemove = TrainingSlot(time = "12:00", duration = 60)

        val initialSchedule = WeeklySchedule(tuesday = listOf(slotToKeep, slotToRemove))
        weeklyScheduleLiveData.value = initialSchedule

        viewModel.removeSlot("Tuesday", slotToRemove)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<WeeklySchedule>()
        coVerify { scheduleRepository.setWeeklySchedule(testUserId, capture(slot)) }

        val updatedTuesday = slot.captured.tuesday!!
        assertEquals(1, updatedTuesday.size)
        assertEquals("10:00", updatedTuesday[0].time)
    }

    @Test
    fun `applyBulkSchedule generates correct slots`() = runTest {
        val config = BulkScheduleConfig(
            selectedDays = setOf("Wednesday", "Friday"),
            startHour = 10,
            startMinute = 0,
            endHour = 13,
            endMinute = 0,
            durationMinutes = 60,
            breakMinutes = 30
        )

        weeklyScheduleLiveData.value = WeeklySchedule()

        viewModel.applyBulkSchedule(config)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<WeeklySchedule>()
        coVerify { scheduleRepository.setWeeklySchedule(testUserId, capture(slot)) }
        val result = slot.captured

        val wedSlots = result.wednesday!!
        assertEquals("Should have 2 slots on Wednesday", 2, wedSlots.size)
        assertEquals("First slot start", "10:00", wedSlots[0].time)
        assertEquals("Second slot start (10:00 + 60m work + 30m break)", "11:30", wedSlots[1].time)

        val friSlots = result.friday!!
        assertEquals("Should have 2 slots on Friday", 2, friSlots.size)
        assertEquals("10:00", friSlots[0].time)

        assertTrue(result.monday.isNullOrEmpty())
    }
}