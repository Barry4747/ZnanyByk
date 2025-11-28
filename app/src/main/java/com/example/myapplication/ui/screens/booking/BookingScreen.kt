package com.example.myapplication.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.viewmodel.booking.BookingViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    trainerId: String,
    onNavigateToPayment: (String, Long, String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    LaunchedEffect(trainerId) {
        viewModel.init(trainerId)
    }

    val state by viewModel.uiState.collectAsState()

val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = remember(state.schedule, state.appointments) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val dateToCheck = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()

                    val today = LocalDate.now()

                    if (dateToCheck.isBefore(today)) return false

                    val rawSlots = when (dateToCheck.dayOfWeek) {
                        DayOfWeek.MONDAY -> state.schedule.monday
                        DayOfWeek.TUESDAY -> state.schedule.tuesday
                        DayOfWeek.WEDNESDAY -> state.schedule.wednesday
                        DayOfWeek.THURSDAY -> state.schedule.thursday
                        DayOfWeek.FRIDAY -> state.schedule.friday
                        DayOfWeek.SATURDAY -> state.schedule.saturday
                        DayOfWeek.SUNDAY -> state.schedule.sunday
                        else -> emptyList()
                    } ?: emptyList()

                    if (rawSlots.isEmpty()) return false

                    val appointmentsOnDay = state.appointments.filter { appt ->
                        val apptDate = appt.date?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate()
                        apptDate == dateToCheck
                    }

                    val isFull = appointmentsOnDay.size >= rawSlots.size

                    return !isFull
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= LocalDate.now().year
                }
            }
        }
    )


    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val selectedDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
            viewModel.onDateSelected(selectedDate)
        }
    }

    Scaffold(
        topBar = { MainTopBar(onNavigateBack = onNavigateBack, text = "Umów trening") },
        bottomBar = {
            MainButton(
                onClick = {
                    state.selectedSlot?.let { slot ->
                        val dateMillis = state.selectedDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        onNavigateToPayment(state.trainerId, dateMillis, slot.time ?: "")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = state.selectedSlot != null,
                text = "Przejdź do płatności"
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(horizontal = 16.dp),
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    disabledDayContentColor = Color.LightGray.copy(alpha = 0.6f),
                    disabledSelectedDayContentColor = Color.Gray,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            HorizontalDivider()

            Text(
                text = "Dostępne godziny:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.availableSlots.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (state.appointments.isNotEmpty()) "Brak wolnych miejsc w tym dniu." else "Wybierz datę.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableSlots) { slot ->
                        val isSelected = slot == state.selectedSlot
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSlotSelected(slot) },
                            label = { Text(slot.time ?: "--:--", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}