package com.example.myapplication.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.viewmodel.booking.BookingViewModel
import java.time.Instant
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
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val selectedDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            viewModel.onDateSelected(selectedDate)
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(onNavigateBack = onNavigateBack, text="Umów wizytę" )
        },
        bottomBar = {
            MainButton(
                onClick = {
                    state.selectedSlot?.let { slot ->
                        val dateMillis = state.selectedDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        onNavigateToPayment(
                            state.trainerId,
                            dateMillis,
                            slot.time ?: ""
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = state.selectedSlot != null,
                text = "Przejdź do płatności"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. Kalendarz
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp),
                showModeToggle = false,
                title = null,
                headline = null
            )

            HorizontalDivider()

            Text(
                text = "Dostępne godziny:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            // 2. Wybór godziny
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.availableSlots.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Brak wolnych terminów w tym dniu.", color = MaterialTheme.colorScheme.secondary)
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
                            label = {
                                Text(
                                    text = slot.time ?: "--:--",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            },
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