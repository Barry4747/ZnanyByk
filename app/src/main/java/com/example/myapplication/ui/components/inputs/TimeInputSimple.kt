package com.example.myapplication.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimeInputSimple(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    Text(
        text = String.format("%02d:%02d", hour, minute),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(8.dp)
            .clickable {
                val newHour = if (hour + 1 > 23) 0 else hour + 1
                onChange(newHour, minute)
            }
    )
}