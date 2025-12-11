package com.example.myapplication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.viewmodel.booking.BookingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val scheduleRepository = mockk<ScheduleRepository>(relaxed = true)
    private val trainerRepository = mockk<TrainerRepository>(relaxed = true)

    private val weeklyScheduleLiveData = MutableLiveData<WeeklySchedule>()
    private val appointmentsLiveData = MutableLiveData<List<Appointment>>()

    private lateinit var viewModel: BookingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { scheduleRepository.weeklySchedule } returns weeklyScheduleLiveData
        every { scheduleRepository.appointments } returns appointmentsLiveData

        mockkStatic(LocalDate::class)
        mockkStatic(LocalTime::class)

        every { LocalDate.now() } returns LocalDate.of(2024, 1, 1)
        every { LocalTime.now() } returns LocalTime.of(12, 0)

        viewModel = BookingViewModel(scheduleRepository, trainerRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(LocalDate::class)
        unmockkStatic(LocalTime::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateSlots filters out taken appointments`() {
        val testDate = LocalDate.of(2024, 5, 20) // Monday
        every { LocalDate.now() } returns testDate
        every { LocalTime.now() } returns LocalTime.of(8, 0)

        val schedule = WeeklySchedule(
            monday = listOf(
                TrainingSlot("10:00"),
                TrainingSlot("11:00"),
                TrainingSlot("12:00")
            )
        )

        val takenDate = Date.from(testDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val appointments = listOf(
            Appointment(date = takenDate, time = "11:00")
        )

        weeklyScheduleLiveData.value = schedule
        appointmentsLiveData.value = appointments

        viewModel.onDateSelected(testDate)

        val slots = viewModel.uiState.value.availableSlots
        assertEquals(2, slots.size)
        assertTrue(slots.any { it.time == "10:00" })
        assertTrue(slots.any { it.time == "12:00" })
        assertTrue(slots.none { it.time == "11:00" })
    }

    @Test
    fun `calculateSlots filters out past hours when date is today`() {
        val today = LocalDate.of(2024, 6, 15)
        every { LocalDate.now() } returns today
        every { LocalTime.now() } returns LocalTime.of(10, 30)

        val schedule = WeeklySchedule(
            saturday = listOf(
                TrainingSlot("09:00"), // Past
                TrainingSlot("10:00"), // Past
                TrainingSlot("11:00"), // Future
                TrainingSlot("12:00")  // Future
            )
        )

        weeklyScheduleLiveData.value = schedule
        appointmentsLiveData.value = emptyList()

        viewModel.onDateSelected(today)

        val slots = viewModel.uiState.value.availableSlots
        assertEquals(2, slots.size)
        assertEquals("11:00", slots[0].time)
        assertEquals("12:00", slots[1].time)
    }

    @Test
    fun `calculateSlots shows all hours when date is in future`() {
        val today = LocalDate.of(2024, 6, 15)
        val tomorrow = today.plusDays(1) // Sunday

        every { LocalDate.now() } returns today
        every { LocalTime.now() } returns LocalTime.of(20, 0) // Late evening today

        val schedule = WeeklySchedule(
            sunday = listOf(
                TrainingSlot("08:00"),
                TrainingSlot("09:00")
            )
        )

        weeklyScheduleLiveData.value = schedule
        appointmentsLiveData.value = emptyList()

        viewModel.onDateSelected(tomorrow)

        val slots = viewModel.uiState.value.availableSlots
        assertEquals(2, slots.size)
    }
}