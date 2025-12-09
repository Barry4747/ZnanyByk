package com.example.myapplication.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.ui.components.inputs.TimeInputSimple
import com.example.myapplication.ui.components.pickers.TimePickerRow
import com.example.myapplication.ui.components.toggles.DayToggle

@Composable
fun BulkScheduleDialog(
    onConfirm: (Set<String>, Int, Int, Int, Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val dayLabels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }

    var endHour by remember { mutableStateOf(16) }
    var endMinute by remember { mutableStateOf(0) }

    var durationText by remember { mutableStateOf("60") }
    var breakText by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generator Harmonogramu") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Wybierz dni:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEachIndexed { index, day ->
                        DayToggle(
                            label = dayLabels[index],
                            isSelected = selectedDays.contains(day),
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            }
                        )
                    }
                }

                HorizontalDivider()

                Text("Zakres godzin (Od - Do):", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeInputSimple(startHour, startMinute) { h, m -> startHour = h; startMinute = m }
                    Text(" - ", modifier = Modifier.padding(horizontal = 8.dp))
                    TimeInputSimple(endHour, endMinute) { h, m -> endHour = h; endMinute = m }
                }

                HorizontalDivider()

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Czas (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = breakText,
                        onValueChange = { breakText = it },
                        label = { Text("Przerwa (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            MainButton(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    val breakTime = breakText.toIntOrNull() ?: 0
                    if (selectedDays.isNotEmpty() && duration > 0) {
                        onConfirm(selectedDays, startHour, startMinute, endHour, endMinute, duration, breakTime)
                    }
                },
                text = "Generuj"
            )
        },
        dismissButton = {
            MainTextButton(onClick = onDismiss, text = "Anuluj")
        }
    )
}



