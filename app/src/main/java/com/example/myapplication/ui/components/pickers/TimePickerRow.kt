package com.example.myapplication.ui.components.pickers

import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TimePickerRow(
    selectedHour: Int,
    onHourChange: (Int) -> Unit,
    selectedMinute: Int,
    onMinuteChange: (Int) -> Unit
) {
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
                    setOnValueChangedListener { _, _, newVal -> onHourChange(newVal) }
                }
            },
            modifier = Modifier
                .width(100.dp)
                .height(120.dp)
        )

        Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))

        AndroidView(
            factory = { ctx ->
                NumberPicker(ctx).apply {
                    minValue = 0
                    maxValue = 59
                    value = selectedMinute
                    setFormatter { i -> String.format("%02d", i) }
                    setOnValueChangedListener { _, _, newVal -> onMinuteChange(newVal) }
                }
            },
            modifier = Modifier
                .width(100.dp)
                .height(120.dp)
        )
    }
}
