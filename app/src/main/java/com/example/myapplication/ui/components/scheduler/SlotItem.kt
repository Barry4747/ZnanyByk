package com.example.myapplication.ui.components.scheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel

@Composable
fun SlotItem(
    day: String,
    slot: TrainingSlot,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    var isSelected by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color.Red.copy(alpha = 0.2f) else Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .clickable { isSelected = !isSelected }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${slot.time} (${slot.duration} min)",
                style = MaterialTheme.typography.bodyMedium
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.removeSlot(day, slot) }
                )
            }
        }
    }
}
