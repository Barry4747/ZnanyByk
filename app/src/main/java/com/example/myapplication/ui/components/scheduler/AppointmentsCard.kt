package com.example.myapplication.ui.components.scheduler

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.DayOfTheWeek
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentCard(
    appointment: Appointment,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val isPast = appointment.date?.before(Date()) ?: false
    val userData = viewModel.getUserById(appointment.trainerId.toString())

    val statusColor = if (isPast) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
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
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = userData?.avatarUrl,
                        contentDescription = stringResource(R.string.profile_picture),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        error = painterResource(R.drawable.user_active)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${userData?.firstName ?: ""} ${userData?.lastName ?: ""}".trim(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = appointment.title ?: "Trening",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                appointment.date?.let { date ->
                    InfoRow(label = "Data", value = dateFormat.format(date))
                }

                appointment.time?.let { time ->
                    InfoRow(label = "Godzina", value = time)
                }

                appointment.duration?.let { duration ->
                    InfoRow(label = "Czas trwania", value = "$duration min")
                }

                if (dayOfWeekText.isNotEmpty()) {
                    InfoRow(label = "Dzień tygodnia", value = dayOfWeekText)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentCardPreview() {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    val upcomingAppointment = Appointment(
        trainerId = "aspi9sAmtVMBPz7fEbodmTl3VI13",
        clientId = "client123",
        date = formatter.parse("20.11.2025 10:00"),
        dayOfWeek = DayOfTheWeek.THURSDAY,
        time = "10:00",
        duration = 60,
        title = "Trening siłowy"
    )

    val pastAppointment = Appointment(
        trainerId = "trainer123",
        clientId = "client123",
        date = formatter.parse("05.11.2025 15:30"),
        dayOfWeek = DayOfTheWeek.WEDNESDAY,
        time = "15:30",
        duration = 45,
        title = "Trening funkcjonalny"
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppointmentCard(appointment = upcomingAppointment)
            AppointmentCard(appointment = pastAppointment)
        }
    }
}
