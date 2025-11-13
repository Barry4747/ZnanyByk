package com.example.myapplication.viewmodel
//todo do wywalenia ???
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Task(
    val title: String,
    val description: String,
    val time: String
)

data class SchedulerState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null
)

class SchedulerViewModel : ViewModel() {

    private val _schedulerState = MutableStateFlow(SchedulerState())
    val schedulerState = _schedulerState.asStateFlow()

    init {
        loadMockTasks()
    }

    fun refreshTasks() {
        loadMockTasks()
    }

    private fun loadMockTasks() {
        viewModelScope.launch {
            _schedulerState.value = SchedulerState(isLoading = true)
            delay(1000) // symulacja ładowania

            // Mockowe dane
            val mockTasks = listOf(
                Task("Spotkanie z zespołem", "Omówienie sprintu", "09:00"),
                Task("Lunch", "Z kolegą z pracy", "12:30"),
                Task("Call z klientem", "Prezentacja projektu", "15:00"),
                Task("Trening", "Siłownia", "18:30")
            )

            _schedulerState.value = SchedulerState(tasks = mockTasks)
        }
    }
}
