package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.TrainerCategory
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.chips.MainCategoryChip
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.TrainerRegistrationViewModel

@Composable
fun TrainerRegistrationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    viewModel: TrainerRegistrationViewModel = hiltViewModel(),
) {
    var hourlyRate by remember { mutableStateOf("") }
    var selectedGym by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<TrainerCategory>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            MainBackButton(
                onClick = onNavigateBack
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = state.userName.ifBlank { stringResource(R.string.placeholder_username) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MainFormTextField(
                value = hourlyRate,
                onValueChange = { input -> hourlyRate = input.filter { it.isDigit() } },
                label = stringResource(R.string.hourly_rate),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormButton(
                text = selectedGym ?: stringResource(R.string.my_gym),
                onClick = { /* TODO: Open gym picker dialog */ },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description_label),
                enabled = !state.isLoading,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = experienceYears,
                onValueChange = { input -> experienceYears = input.filter { it.isDigit() } },
                label = stringResource(R.string.how_long_trainer),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.height(56.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedCategories) { category ->
                        MainCategoryChip(category = category)
                    }
                    item {
                        MainCategoryChip(label = "+", onClick = { showDialog = true })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PLACEHOLDER - ${stringResource(R.string.files)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.confirm_trainer_profile_creation),
                    onClick = {
                        viewModel.submitTrainerProfile(
                            hourlyRate = hourlyRate,
                            gymId = selectedGym,
                            description = description,
                            experienceYears = experienceYears,
                            selectedCategories = selectedCategories.map { it.name }
                        )
                        onSubmit()
                    },
                    enabled = hourlyRate.isNotBlank() && description.isNotBlank() && experienceYears.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = Color.Red
                )
            }

            if (state.successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.successMessage ?: "",
                    color = Color.Green
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Wybierz kategorie") },
            text = {
                LazyColumn {
                    items(TrainerCategory.values()) { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick = {
                                selectedCategories = if (category in selectedCategories) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            },
                            label = { Text(stringResource(category.labelRes)) }
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainerRegistrationScreenPreview() {
    TrainerRegistrationScreen()
}