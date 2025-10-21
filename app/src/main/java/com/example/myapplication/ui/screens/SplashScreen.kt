package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToStart: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var hasStartedChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
    }

    LaunchedEffect(authState.isCheckingAuth, authState.user) {
        if (authState.isCheckingAuth) {
            hasStartedChecking = true
        }

        if (hasStartedChecking && !authState.isCheckingAuth) {
            if (authState.user != null) {
                onNavigateToHome()
            } else {
                onNavigateToStart()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Loading...")
    }
}
