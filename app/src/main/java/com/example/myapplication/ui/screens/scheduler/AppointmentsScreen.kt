package com.example.myapplication.ui.screens.scheduler

import MonthYearPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.scheduler.AppointmentCard
import java.time.LocalDate

@Composable
fun AppointmentsScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var selectedMonthYear by remember { mutableStateOf(LocalDate.now()) }

    val appointments by viewModel.appointments.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadAppointmentsForMonthYear(
            selectedMonthYear.monthValue,
            selectedMonthYear.year
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_workouts),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FormButton(
                text = "${selectedMonthYear.monthValue}.${selectedMonthYear.year}",
                onClick = { showMonthYearPicker = true },
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (appointments.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_workouts_this_month),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appointments.sortedBy { it.date }) { appointment ->
                        AppointmentCard(appointment = appointment)
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPicker(
            initialMonth = selectedMonthYear.monthValue,
            initialYear = selectedMonthYear.year,
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { month, year ->
                selectedMonthYear = LocalDate.of(year, month, 1)
                viewModel.loadAppointmentsForMonthYear(month, year)
                showMonthYearPicker = false
            }
        )
    }
}


