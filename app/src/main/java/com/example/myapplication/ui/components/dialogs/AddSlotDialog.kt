package com.example.myapplication.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.pickers.TimePickerRow

@Composable
fun AddSlotDialog(
    day: String,
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var durationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nowy slot - $day") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePickerRow(
                    selectedHour = selectedHour,
                    onHourChange = { selectedHour = it },
                    selectedMinute = selectedMinute,
                    onMinuteChange = { selectedMinute = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Czas trwania (minuty)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val duration = durationText.toIntOrNull() ?: 0
                if (duration > 0) {
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onConfirm(formattedTime, duration)
                }
            }) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
