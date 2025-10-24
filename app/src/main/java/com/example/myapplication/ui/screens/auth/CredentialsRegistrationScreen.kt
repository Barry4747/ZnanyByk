package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.GoogleAuthButton
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.rejestracja),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            enabled = !registrationState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.has_o)) },
            enabled = !registrationState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registrationState.isLoading) {
            CircularProgressIndicator()
        } else {
            MainButton(
                text = stringResource(R.string.kontynuuj),
                onClick = {
                    viewModel.saveRegistrationCredentials(email, password)
                    onNavigateToPersonalInfo()
                },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoogleAuthButton(
                text = stringResource(R.string.kontynuuj_z_google),
                onClick = { viewModel.signUpWithGoogle() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = stringResource(R.string.masz_ju_konto_zaloguj_si),
            onClick = onNavigateToLogin,
            enabled = !registrationState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = stringResource(R.string.wr_do_ekranu_startowego),
            onClick = onNavigateBack,
            enabled = !registrationState.isLoading
        )
    }
}
