package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.GoogleAuthButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.viewmodel.RegistrationViewModel

@Composable
fun CredentialsRegistrationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onGoogleSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val registrationState by viewModel.registrationState.collectAsState()

    LaunchedEffect(registrationState.user) {
        if (registrationState.user != null) {
            onGoogleSignInSuccess()
        }
    }

    LaunchedEffect(registrationState.pendingGoogleUid) {
        if (registrationState.pendingGoogleUid != null) {
            onNavigateToPersonalInfo()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MainBackButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.znanybyklogo_transparent),
                contentDescription = stringResource(R.string.znany_byk_logo),
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.registration),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = registrationState.registrationCredentials.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text(stringResource(R.string.email)) },
                enabled = !registrationState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = registrationState.registrationCredentials.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text(stringResource(R.string.password)) },
                enabled = !registrationState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                isError = registrationState.passwordValidationError != null &&
                        registrationState.registrationCredentials.password.isNotBlank(),
                supportingText = if (registrationState.passwordValidationError != null &&
                    registrationState.registrationCredentials.password.isNotBlank()) {
                    { Text(registrationState.passwordValidationError ?: "") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = registrationState.registrationCredentials.repeatPassword,
                onValueChange = { viewModel.updateRepeatPassword(it) },
                label = { Text(stringResource(R.string.repeat_password)) },
                enabled = !registrationState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                isError = registrationState.passwordValidationError != null &&
                        registrationState.registrationCredentials.repeatPassword.isNotBlank(),
                supportingText = if (registrationState.passwordValidationError != null &&
                    registrationState.registrationCredentials.repeatPassword.isNotBlank()) {
                    { Text(registrationState.passwordValidationError ?: "") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (registrationState.isLoading) {
                CircularProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.continue_msg),
                    onClick = {
                        viewModel.saveRegistrationCredentials(
                            registrationState.registrationCredentials.email,
                            registrationState.registrationCredentials.password
                        )
                        onNavigateToPersonalInfo()
                    },
                    enabled = viewModel.isFormValid(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GoogleAuthButton(
                    text = stringResource(R.string.continue_with_google),
                    onClick = { viewModel.signUpWithGoogle() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MainTextButton(
                text = stringResource(R.string.alr_have_an_acc_login),
                onClick = onNavigateToLogin,
                enabled = !registrationState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainTextButton(
                text = stringResource(R.string.back_to_start_screen),
                onClick = onNavigateBack,
                enabled = !registrationState.isLoading
            )
        }
    }
}
