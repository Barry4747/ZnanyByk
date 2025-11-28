package com.example.myapplication.ui.screens.auth

import MainProgressIndicator
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.commandiron.wheel_picker_compose.WheelDatePicker
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.example.myapplication.R
import com.example.myapplication.data.model.CountryCodes
import com.example.myapplication.ui.components.OrDivider
import com.example.myapplication.ui.components.buttons.AlternateButton
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.dialogs.CountryCodePickerDialog
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.registration.RegistrationViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PersonalInfoRegistrationScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    onRegistrationSuccessProceedWithTrainer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val registrationState by viewModel.registrationState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(CountryCodes.default) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }
    var wantsToBeTrainer by remember { mutableStateOf(false) }
    val emailPrefill = registrationState.registrationCredentials.email

    var showDatePicker by remember { mutableStateOf(false) }
    var snappedDate by remember { mutableStateOf(LocalDateTime.now().minusYears(25).toLocalDate()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(phoneNumber, selectedCountry) {
        if (phoneNumber.isNotBlank()) {
            viewModel.updatePhoneNumber(phoneNumber, selectedCountry.dialCode)
        }
    }

    LaunchedEffect(registrationState.errorMessage) {
        registrationState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
        }
    }

    if (registrationState.successMessage != null) {
        if (wantsToBeTrainer) {
            onRegistrationSuccessProceedWithTrainer()
        } else {
            onRegistrationSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize().imePadding()) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.personal_info),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            MainFormTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = stringResource(R.string.first_name),
                enabled = !registrationState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = stringResource(R.string.last_name),
                enabled = !registrationState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = emailPrefill,
                onValueChange = {},
                label = stringResource(R.string.email),
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                FormButton(
                    text = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                    onClick = { showCountryPicker = true },
                    modifier = Modifier
                        .width(120.dp)
                        .padding(top = 8.dp),
                    enabled = !registrationState.isLoading
                )

                Spacer(modifier = Modifier.width(8.dp))
                
                MainFormTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it.filter { char -> char.isDigit() }
                    },
                    label = stringResource(R.string.phone_number_opt),
                    enabled = !registrationState.isLoading,
                    isError = registrationState.phoneNumberValidationError != null && phoneNumber.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            
            if (registrationState.phoneNumberValidationError != null && phoneNumber.isNotBlank()) {
                Text(
                    text = registrationState.phoneNumberValidationError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FormButton(
                text = birthDateMillis?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                } ?: stringResource(R.string.birthday_opt),
                onClick = { showDatePicker = true },
                enabled = !registrationState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (registrationState.isLoading) {
                MainProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.create_account),
                    onClick = {
                        wantsToBeTrainer = false
                        val birthDate = birthDateMillis?.let { Date(it) }

                        viewModel.register(
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = if (phoneNumber.isNotBlank()) "${selectedCountry.dialCode}$phoneNumber" else null,
                            birthDate = birthDate
                        )
                    },
                    enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OrDivider()

                AlternateButton(
                    text = stringResource(R.string.want_to_be_trainer),
                    onClick = {
                        wantsToBeTrainer = true
                        val birthDate = birthDateMillis?.let { Date(it) }

                        viewModel.register(
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = if (phoneNumber.isNotBlank()) "${selectedCountry.dialCode}$phoneNumber" else null,
                            birthDate = birthDate
                        )
                    },
                    enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        MainBackButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showCountryPicker) {
        CountryCodePickerDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { selectedCountry = it },
            onDismiss = { showCountryPicker = false }
        )
    }

    if (showDatePicker) {
        LaunchedEffect(showDatePicker) {
            birthDateMillis?.let {
                snappedDate = Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            } ?: run {
                snappedDate = LocalDateTime.now().minusYears(25).toLocalDate()
            }
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
                        text = stringResource(R.string.choose_birth_date),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    WheelDatePicker(
                        startDate = snappedDate,
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
                        snappedDate = snappedDateValue // <-- update controlled state
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            val millis =
                                snappedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                                    .toEpochMilli()
                            birthDateMillis = millis
                            showDatePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}
