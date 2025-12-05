package com.example.myapplication.ui.screens.booking

import MainProgressIndicator
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    onNavigateToPayment: (String, Long, String, String) -> Unit,
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
                    return appointmentsOnDay.size < rawSlots.size
                }

                override fun isSelectableYear(year: Int) = year >= LocalDate.now().year
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

    val isButtonEnabled = state.selectedSlot != null && state.selectedCategory != null

    Scaffold(
        containerColor = Color.White,
        topBar = {
            MainTopBar(text="Rezerwacja", onNavigateBack = onNavigateBack)
                 },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                MainButton(
                    onClick = {
                        state.selectedSlot?.let { slot ->
                            state.selectedCategory?.let { category ->
                                val dateMillis = state.selectedDate
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                onNavigateToPayment(state.trainerId, dateMillis, slot.time ?: "", category)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isButtonEnabled,
                    text = "Przejdź do płatności"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(horizontal = 8.dp),
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = Color.Black,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color.Black,
                    todayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    weekdayContentColor = Color.Gray,
                    currentYearContentColor = Color.Black,
                    selectedYearContainerColor = Color.Black,
                    selectedYearContentColor = Color.White,
                    disabledDayContentColor = Color.LightGray.copy(alpha = 0.4f),
                    disabledSelectedDayContentColor = Color.LightGray
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 1.dp,
                color = Color(0xFFF0F0F0)
            )

            AnimatedVisibility(
                visible = state.categories.isNotEmpty(),
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Text(
                        text = "RODZAJ TRENINGU",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.categories) { category ->
                            val isSelected = state.selectedCategory == category

                            Surface(
                                selected = isSelected,
                                onClick = { viewModel.onCategorySelected(category) },
                                shape = CircleShape,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                color = if (isSelected) Color.Black else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black,
                                modifier = Modifier.height(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "DOSTĘPNE GODZINY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.Gray,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    MainProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.availableSlots.isEmpty()) {
                    Text(
                        text = if (state.appointments.isNotEmpty()) "Brak terminów" else "Wybierz datę",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 85.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.availableSlots) { slot ->
                            val isSelected = slot == state.selectedSlot

                            Surface(
                                selected = isSelected,
                                onClick = { viewModel.onSlotSelected(slot) },
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                color = if (isSelected) Color.Black else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black,
                                modifier = Modifier
                                    .height(48.dp)
                                    .animateContentSize()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = slot.time ?: "--:--",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}