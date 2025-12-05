package com.example.myapplication.ui.components.scheduler

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.DayOfTheWeek
import com.example.myapplication.data.model.users.User
import com.example.myapplication.ui.components.buttons.MessageButton
import com.example.myapplication.utils.calculateAppointmentStatus
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onAppointmentChatClick: (chatId: String, receiverId: String) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val (isPast, isToday) = remember(appointment) {
        calculateAppointmentStatus(appointment)
    }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val currentUserId = viewModel.currentUserId.toString()
    val isTrainer = currentUserId == appointment.trainerId.toString()
    val targetUserId = if (isTrainer) appointment.clientId else appointment.trainerId
    var userData by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(targetUserId) {
        userData = viewModel.getUserById(targetUserId.toString())
    }

    val gym by produceState<Gym?>(initialValue = null, key1 = appointment.gymId) {
        appointment.gymId?.let { id ->
            value = viewModel.getGymById(id)
        }
    }

    val statusColor = if (isPast) Color(0xFFBE3737) else if (isToday) Color(0xFF4CAF50) else Color.Black
    val statusText = if (isPast) stringResource(R.string.finished) else if (isToday) stringResource(
        R.string.today
    ) else stringResource(R.string.planned)
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
                Row (verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        painter = painterResource(R.drawable.scheduler_grey),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$dayOfWeekText, ${dateFormat.format(appointment.date ?: Date())}"
                    )
                }
                Row (verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        painter = painterResource(R.drawable.clock),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${appointment.time}, ${appointment.duration} min"
                    )
                }
                Row (verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        painter = painterResource(R.drawable.location_mark),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val location = gym?.gymLocation?.formattedAddress
                    Text(
                        text = location?: "Brak informacji"
                    )
                }
            }

            MessageButton(
                userId = appointment.clientId.toString(),
                trainerId = appointment.trainerId.toString(),
                onAppointmentChatClick = onAppointmentChatClick
            )
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
            AppointmentCard(appointment = upcomingAppointment, onAppointmentChatClick = {} as (String, String) -> Unit)
            AppointmentCard(appointment = pastAppointment, onAppointmentChatClick = {} as (String, String) -> Unit)
        }
    }
}
