package com.example.myapplication.ui.components.scheduler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.trainings.WeeklySchedule

@Composable
fun ScheduleList(
    days: List<String>,
    weeklySchedule: WeeklySchedule?, // typ z ViewModelu
    onAddClick: (String) -> Unit
) {
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
                onAddClick = { onAddClick(day) }
            )
        }
    }
}
