package com.example.myapplication.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.PaymentButton
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
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val dateString = remember(dateMillis) {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("pl", "PL"))
        formatter.format(Date(dateMillis))
    }

    LaunchedEffect(state) {
        if (state is PaymentUiState.Success) {
            kotlinx.coroutines.delay(1500)
            onPaymentSuccess()
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(onNavigateBack = onNavigateBack, text = "Płatność")
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (state is PaymentUiState.Success) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Płatność przyjęta!", style = MaterialTheme.typography.headlineMedium)
                    Text("Trening został umówiony.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Szczegóły rezerwacji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            RowItem("Trener ID", trainerId.take(8) + "...")
                            RowItem("Data", dateString)
                            RowItem("Godzina", time)
                            RowItem("Czas trwania", "60 min")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            RowItem("Do zapłaty", "120.00 PLN", isBold = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Metoda płatności", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Karta **** 4242 (Mock)")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (state is PaymentUiState.Error) {
                        Text(
                            text = (state as PaymentUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    PaymentButton(
                        isProcessing = state is PaymentUiState.Processing,
                        onClick = {
                            viewModel.processPayment(trainerId, dateMillis, time)
                        }
                    )

                }
            }
        }
    }
}

@Composable
fun RowItem(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isBold) 18.sp else 16.sp
        )
    }
}