package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.viewmodel.profile.LocationOnboardingViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction

@Composable
fun LocationOnboardingScreen(
    viewModel: LocationOnboardingViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaveComplete) {
        if (uiState.isSaveComplete) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                MainBackButton(onClick = onNavigateBack)
            }

            Text(
                text = "Gdzie się znajdujesz?",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "To pomaga nam znaleźć trenerów w Twojej okolicy.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box {
                Column {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        label = { Text("Wprowadź swój adres lub miasto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (uiState.predictions.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            items(uiState.predictions) { prediction ->
                                PredictionItem(
                                    prediction = prediction,
                                    onClick = {
                                        viewModel.onPredictionSelected(prediction)
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                MainButton(
                    text = "Zapisz i kontynuuj",
                    onClick = viewModel::onSaveLocation,
                    enabled = uiState.selectedPlace != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    Text(
        text = prediction.getFullText(null).toString(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 12.dp)
    )
}