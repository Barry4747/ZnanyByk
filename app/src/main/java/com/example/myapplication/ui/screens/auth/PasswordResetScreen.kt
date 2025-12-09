package com.example.myapplication.ui.screens.auth

import MainProgressIndicator
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.registration.AuthViewModel
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PasswordResetScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val emailState = remember { mutableStateOf("") }

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
            with(sharedTransitionScope) {
                Image(
                    painter = painterResource(id = R.drawable.znanybyklogo_transparent),
                    contentDescription = stringResource(R.string.znany_byk_logo),
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "logo"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = ANIMATION_TIME, easing = FastOutSlowInEasing)
                            }
                        )
                        .size(270.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resetowanie hasła",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MainFormTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = stringResource(R.string.email),
                enabled = !authState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (authState.isLoading) {
                MainProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.send_password_reset_email),
                    onClick = { viewModel.resetPassword(emailState.value) },
                    enabled = emailState.value.isNotBlank(),
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

            if (authState.passwordResetMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authState.passwordResetMessage ?: "",
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
