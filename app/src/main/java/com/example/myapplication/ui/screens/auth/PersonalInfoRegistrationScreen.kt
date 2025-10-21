package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.utils.parseBirthDate
import com.example.myapplication.viewmodel.RegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoRegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val registrationState by viewModel.registrationState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    val emailPrefill = registrationState.registrationCredentials.email

    if (registrationState.successMessage != null) {
        onRegistrationSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dane Personalne",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Imię") },
            enabled = !registrationState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Nazwisko") },
            enabled = !registrationState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emailPrefill,
            onValueChange = {},
            label = { Text("Email") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Numer telefonu (opcjonalne)") },
            enabled = !registrationState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDateText,
            onValueChange = {
                birthDateText = it
                val result = parseBirthDate(it)
                birthDateError = result.error
            },
            label = { Text("Data urodzenia (opcjonalne)") },
            placeholder = { Text("dd/MM/yyyy") },
            enabled = !registrationState.isLoading,
            isError = birthDateError != null,
            supportingText = {
                if (birthDateError != null) {
                    Text(
                        text = birthDateError!!,
                        color = Color.Red
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registrationState.isLoading) {
            CircularProgressIndicator()
        } else {
            MainButton(
                text = "Utwórz konto",
                onClick = {
                    val birthDate = parseBirthDate(birthDateText).date

                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber.ifBlank { null },
                        birthDate = birthDate
                    )
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && (birthDateText.isBlank() || birthDateError == null),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (registrationState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = registrationState.errorMessage ?: "",
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = "Wróć",
            onClick = onNavigateBack,
            enabled = !registrationState.isLoading
        )
    }
}
