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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.DayOfTheWeek
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@Composable
fun FormButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}

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
                    text = "Moje treningi",
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
                    text = "Brak treningów w wybranym miesiącu",
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

@Composable
fun AppointmentCard(appointment: Appointment) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val isPast = appointment.date?.before(Date()) ?: false

    val statusColor = if (isPast) {
        Color(0xFF4CAF50)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val statusText = if (isPast) "Zakończony" else "Zaplanowany"

    val dayOfWeekText = when (appointment.dayOfWeek) {
        DayOfTheWeek.MONDAY -> "Poniedziałek"
        DayOfTheWeek.TUESDAY -> "Wtorek"
        DayOfTheWeek.WEDNESDAY -> "Środa"
        DayOfTheWeek.THURSDAY -> "Czwartek"
        DayOfTheWeek.FRIDAY -> "Piątek"
        DayOfTheWeek.SATURDAY -> "Sobota"
        DayOfTheWeek.SUNDAY -> "Niedziela"
        null -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.title ?: "Trening",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (dayOfWeekText.isNotEmpty()) {
                        Text(
                            text = dayOfWeekText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                appointment.date?.let { date ->
                    Column {
                        Text(
                            text = "Data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(date),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                appointment.time?.let { time ->
                    Column {
                        Text(
                            text = "Godzina",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                appointment.duration?.let { duration ->
                    Column {
                        Text(
                            text = "Czas trwania",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$duration min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
