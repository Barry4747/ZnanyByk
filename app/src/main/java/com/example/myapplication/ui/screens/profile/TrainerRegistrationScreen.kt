package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.ui.components.fields.MainFormTextField

@Composable
fun TrainerRegistrationScreen(
    onNavigateBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hourlyRate by remember { mutableStateOf("") }
    var selectedGym by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                text = stringResource(R.string.placeholder_username),
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
                onValueChange = { hourlyRate = it },
                label = stringResource(R.string.hourly_rate),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormButton(
                text = selectedGym ?: stringResource(R.string.my_gym),
                onClick = { /* TODO: Open gym picker dialog */ },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description_label),
                enabled = !isLoading,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = stringResource(R.string.how_long_trainer),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PLACEHOLDER - ${stringResource(R.string.categories)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

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

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.confirm_trainer_profile_creation),
                    onClick = {
                        // TODO: Implement submit logic
                        onSubmit()
                    },
                    enabled = hourlyRate.isNotBlank() && description.isNotBlank() && experienceYears.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainerRegistrationScreenPreview() {
    TrainerRegistrationScreen()
}