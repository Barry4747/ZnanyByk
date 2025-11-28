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
import com.example.myapplication.ui.components.dialogs.AddSlotDialog
import com.example.myapplication.ui.components.scheduler.ScheduleCard
import com.example.myapplication.ui.components.scheduler.ScheduleList
import com.example.myapplication.ui.components.scheduler.ScheduleTopBar
import com.example.myapplication.viewmodel.trainer.ScheduleViewModel

@Composable
fun TrainerScheduleScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
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

    Column(modifier = modifier.fillMaxSize()) {
        ScheduleTopBar(onNavigateBack=onNavigateBack)
        ScheduleList(
            days = days,
            weeklySchedule = weeklySchedule,
            onAddClick = { day ->
                selectedDay = day
                showDialog = true
            }
        )
    }

    if (showDialog) {
        AddSlotDialog(
            day = selectedDay,
            onConfirm = { time, duration ->
                viewModel.addNewSlot(selectedDay, TrainingSlot(time, duration))
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}
