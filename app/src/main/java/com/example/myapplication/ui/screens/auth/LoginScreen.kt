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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.MainButton
import com.example.myapplication.ui.components.MainTextButton
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
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    authState.user?.let { _ ->
        onLoginSuccess()
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
            text = "Logowanie",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            enabled = !authState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            MainButton(
                text = "Logowanie",
                onClick = {
                    viewModel.login(email, password)
                },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val webClientId = context.getString(R.string.default_web_client_id)
                    viewModel.signInWithGoogle(webClientId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kontynuuj z Google")
            }
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
            text = "Nie masz konta? Zarejestruj się",
            onClick = onNavigateToRegister,
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainTextButton(
            text = "Wróć do ekranu startowego",
            onClick = onNavigateBack,
            enabled = !authState.isLoading
        )
    }
}
