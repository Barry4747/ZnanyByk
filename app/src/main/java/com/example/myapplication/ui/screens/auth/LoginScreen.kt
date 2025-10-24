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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.GoogleAuthButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(authState.pendingGoogleUid) {
        if (authState.pendingGoogleUid != null) {
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
            text = stringResource(R.string.logowanie),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            enabled = !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.has_o)) },
            enabled = !authState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            MainButton(
                text = stringResource(R.string.email),
                onClick = { viewModel.login(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoogleAuthButton(
                text = stringResource(R.string.kontynuuj_z_google),
                onClick = { viewModel.signInWithGoogle() },
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (authState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = authState.errorMessage ?: "",
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = stringResource(R.string.nie_masz_konta_zarejestruj),
            onClick = onNavigateToRegister,
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = stringResource(R.string.wr_do_ekranu_startowego),
            onClick = onNavigateBack,
            enabled = !authState.isLoading
        )
    }
}
