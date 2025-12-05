package com.example.myapplication.ui.screens.booking

import MainProgressIndicator
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.PaymentButton
import com.example.myapplication.ui.components.payment.PaymentFormView
import com.example.myapplication.ui.components.payment.SuccessView
import com.example.myapplication.viewmodel.booking.PaymentUiState
import com.example.myapplication.viewmodel.booking.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    trainerId: String,
    dateMillis: Long,
    time: String,
    title: String,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val dateString = remember(dateMillis) {
        val formatter = SimpleDateFormat("EEEE, d MMMM", Locale("pl", "PL"))
        val date = Date(dateMillis)
        formatter.format(date).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(state) {
        if (state is PaymentUiState.Success) {
            kotlinx.coroutines.delay(2000)
            onPaymentSuccess()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            MainTopBar(text="Podsumowanie", onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "PaymentStateTransition"
            ) { currentState ->
                when (currentState) {
                    is PaymentUiState.Success -> {
                        SuccessView()
                    }
                    else -> {
                        PaymentFormView(
                            title = title,
                            dateString = dateString,
                            time = time,
                            isLoading = currentState is PaymentUiState.Processing,
                            errorMessage = (currentState as? PaymentUiState.Error)?.message,
                            onPayClick = {
                                viewModel.processPayment(trainerId, dateMillis, time, title)
                            }
                        )
                    }
                }
            }
        }
    }
}