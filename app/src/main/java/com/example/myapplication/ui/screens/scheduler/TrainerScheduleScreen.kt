@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.ui.screens.scheduler

import android.widget.NumberPicker
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.ui.components.scheduler.ScheduleCard
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel

@Composable
fun TrainerScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadSchedule()
        viewModel.loadAppointments()
    }
    val weeklySchedule by viewModel.weeklySchedule.observeAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    var showDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("") }
    var newDuration by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Terminarz", style = MaterialTheme.typography.titleLarge)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(days) { day ->
                val slots = when (day.lowercase()) {
                    "monday" -> weeklySchedule?.monday ?: emptyList()
                    "tuesday" -> weeklySchedule?.tuesday ?: emptyList()
                    "wednesday" -> weeklySchedule?.wednesday ?: emptyList()
                    "thursday" -> weeklySchedule?.thursday ?: emptyList()
                    "friday" -> weeklySchedule?.friday ?: emptyList()
                    "saturday" -> weeklySchedule?.saturday ?: emptyList()
                    "sunday" -> weeklySchedule?.sunday ?: emptyList()
                    else -> emptyList()
                }.sortedBy { it.time }

                ScheduleCard(
                    day = day,
                    slots = slots,
                    onAddClick = {
                        selectedDay = day
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog) {
        var selectedHour by remember { mutableStateOf(12) }
        var selectedMinute by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nowy slot - $selectedDay") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 23
                                    value = selectedHour
                                    wrapSelectorWheel = true
                                    setOnValueChangedListener { _, _, newVal ->
                                        selectedHour = newVal
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(120.dp)
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        AndroidView(
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 59
                                    value = selectedMinute
                                    wrapSelectorWheel = true
                                    setFormatter { i -> String.format("%02d", i) }
                                    setOnValueChangedListener { _, _, newVal ->
                                        selectedMinute = newVal
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("Czas trwania (minuty)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val duration = newDuration.toIntOrNull() ?: 0
                    if (duration > 0 && selectedDay.isNotEmpty()) {
                        val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                        viewModel.addNewSlot(selectedDay, TrainingSlot(formattedTime, duration))
                        showDialog = false
                        newDuration = ""
                    }
                }) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainerScheduleScreenPreview() {
    TrainerScheduleScreen()
}
