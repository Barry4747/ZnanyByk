package com.example.myapplication.ui.screens.auth

import MainProgressIndicator
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.OrDivider
import com.example.myapplication.ui.components.buttons.GoogleAuthButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.MainTextButton
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.registration.RegistrationViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CredentialsRegistrationScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onGoogleSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val registrationState by viewModel.registrationState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var repeatPasswordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                        .size(210.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.registration),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            MainFormTextField(
                value = registrationState.registrationCredentials.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = stringResource(R.string.email),
                enabled = !registrationState.isLoading,
                singleLine = true,
                isError = registrationState.emailValidationError != null &&
                        registrationState.registrationCredentials.email.isNotBlank(),
                supportingText = if (registrationState.emailValidationError != null &&
                    registrationState.registrationCredentials.email.isNotBlank())
                    registrationState.emailValidationError else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = registrationState.registrationCredentials.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = stringResource(R.string.password),
                enabled = !registrationState.isLoading,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                isError = registrationState.passwordValidationError != null &&
                        registrationState.registrationCredentials.password.isNotBlank(),
                supportingText = if (registrationState.passwordValidationError != null &&
                    registrationState.registrationCredentials.password.isNotBlank())
                    registrationState.passwordValidationError else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = registrationState.registrationCredentials.repeatPassword,
                onValueChange = { viewModel.updateRepeatPassword(it) },
                label = stringResource(R.string.repeat_password),
                enabled = !registrationState.isLoading,
                singleLine = true,
                visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    val image = if (repeatPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (registrationState.isLoading) {
                MainProgressIndicator()
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

                Spacer(modifier = Modifier.height(16.dp))

                OrDivider()

                Spacer(modifier = Modifier.height(16.dp))

                GoogleAuthButton(
                    text = stringResource(R.string.continue_with_google),
                    onClick = { viewModel.signUpWithGoogle() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MainTextButton(
                text = stringResource(R.string.alr_have_an_acc_login),
                onClick = onNavigateToLogin,
                enabled = !registrationState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
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
}