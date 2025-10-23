package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.commandiron.wheel_picker_compose.WheelDatePicker
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.RegistrationViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

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
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
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

        MainFormTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = "Imię",
            enabled = !registrationState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainFormTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = "Nazwisko",
            enabled = !registrationState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainFormTextField(
            value = emailPrefill,
            onValueChange = {},
            label = "Email",
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainFormTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = "Numer telefonu (opcjonalne)",
            enabled = !registrationState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormButton(
            text = birthDateMillis?.let {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Wybierz datę urodzenia (opcjonalne)",
            onClick = { showDatePicker = true },
            enabled = !registrationState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registrationState.isLoading) {
            CircularProgressIndicator()
        } else {
            MainButton(
                text = "Utwórz konto",
                onClick = {
                    val birthDate = birthDateMillis?.let { Date(it) }

                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber.ifBlank { null },
                        birthDate = birthDate
                    )
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank(),
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

    if (showDatePicker) {
        var snappedDate by remember {
            mutableStateOf(
                LocalDateTime.now().minusYears(25).toLocalDate()
            )
        }

        Dialog(onDismissRequest = { showDatePicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Wybierz datę urodzenia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    WheelDatePicker(
                        startDate = LocalDateTime.now().minusYears(25).toLocalDate(),
                        minDate = LocalDateTime.of(1900, 1, 1, 0, 0).toLocalDate(),
                        maxDate = LocalDateTime.now().toLocalDate(),
                        size = DpSize(280.dp, 150.dp),
                        rowCount = 5,
                        textStyle = MaterialTheme.typography.titleMedium,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        selectorProperties = WheelPickerDefaults.selectorProperties(
                            enabled = true,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        )
                    ) { snappedDateValue ->
                        snappedDate = snappedDateValue
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Anuluj")
                        }
                        TextButton(onClick = {
                            val millis =
                                snappedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                                    .toEpochMilli()
                            birthDateMillis = millis
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

