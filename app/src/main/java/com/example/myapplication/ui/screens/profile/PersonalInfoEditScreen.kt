package com.example.myapplication.ui.screens.profile

import MainProgressIndicator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.commandiron.wheel_picker_compose.WheelDatePicker
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.example.myapplication.R
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.profile.PInfoEditViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoEditScreen(
    onNavigateBack: () -> Unit,
    onEditSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PInfoEditViewModel = hiltViewModel()
) {
    val personalInfoState by viewModel.state.collectAsState()

    var firstName by remember { mutableStateOf(personalInfoState.firstName) }
    var lastName by remember { mutableStateOf(personalInfoState.lastName) }
    var phoneNumber by remember { mutableStateOf(personalInfoState.phoneNumber) }
    var birthDateMillis by remember { mutableStateOf(personalInfoState.birthDate?.time) }
    val emailPrefill = personalInfoState.email

    var showDatePicker by remember { mutableStateOf(false) }
    var snappedDate by remember { mutableStateOf(LocalDateTime.now().minusYears(25).toLocalDate()) }

    LaunchedEffect(personalInfoState.successMessage) {
        if (personalInfoState.successMessage != null) {
            onEditSuccess()
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                onNavigateBack = onNavigateBack,
                text = stringResource(R.string.edit_personal_info)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            MainFormTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = stringResource(R.string.first_name),
                enabled = false,
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = stringResource(R.string.last_name),
                enabled = false,
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = emailPrefill,
                onValueChange = {},
                label = stringResource(R.string.email),
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = stringResource(R.string.phone_number_opt),
                enabled = !personalInfoState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormButton(
                text = birthDateMillis?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                } ?: stringResource(R.string.birthday_opt),
                onClick = { showDatePicker = true },
                enabled = !personalInfoState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (personalInfoState.isLoading) {
                MainProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.save_changes),
                    onClick = { viewModel.saveChanges(firstName, lastName, phoneNumber, birthDateMillis?.let { Date(it) }, personalInfoState.location, personalInfoState.email) },
                    enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (personalInfoState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = personalInfoState.errorMessage ?: "",
                    color = Color.Red
                )
            }
        }
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
                        snappedDate = snappedDateValue
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
