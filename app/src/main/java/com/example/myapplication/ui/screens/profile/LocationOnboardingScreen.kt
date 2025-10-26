package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.viewmodel.profile.LocationOnboardingViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction

@Composable
fun LocationOnboardingScreen(
    viewModel: LocationOnboardingViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(context, "YOUR_API_KEY_HERE")
        }
    }

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

    // --- Navigation on Success ---
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
            Text(
                text = "Where are you located?",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "This helps us find trainers near you.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Search Box and Predictions ---
            Box {
                Column {
                    // 1. The Search Text Field
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        label = { Text("Enter your address or city") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 2. The Predictions Dropdown
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
                Button(
                    onClick = {}, // onClick = viewModel::onSaveLocation,
                    enabled = uiState.selectedPlace != null, // Only enable if a place is selected
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Save & Continue")
                }
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


@Preview(showBackground = true)
@Composable
fun LocationOnboardingScreenPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LocationOnboardingScreen(onNavigateToDashboard = {})
        }
    }
}
