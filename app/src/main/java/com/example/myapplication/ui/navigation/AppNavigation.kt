package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myapplication.ui.screens.StartScreen
import com.example.myapplication.ui.screens.auth.CredentialsRegistrationScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.PersonalInfoRegistrationScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sharedAuthViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(Screen.Welcome.route) {
            StartScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate("registration_flow")
                }
            )
        }

        composable(Screen.Login.route) { backStackEntry ->
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate("registration_flow") {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onNavigateToPersonalInfo = {
                    navController.navigate("registration_flow") {
                        popUpTo(Screen.Welcome.route)
                    }
                    navController.navigate(Screen.PersonalInfo.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                viewModel = sharedAuthViewModel
            )
        }

        navigation(
            startDestination = Screen.Register.route,
            route = "registration_flow"
        ) {
            composable(Screen.Register.route) { backStackEntry ->
                CredentialsRegistrationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route)
                        }
                    },
                    onNavigateToPersonalInfo = {
                        navController.navigate(Screen.PersonalInfo.route)
                    },
                    viewModel = sharedAuthViewModel
                )
            }

            composable(Screen.PersonalInfo.route) { backStackEntry ->
                PersonalInfoRegistrationScreen(
                    onNavigateBack = {
                        sharedAuthViewModel.clearPendingGoogleRegistration()
                        navController.popBackStack()
                    },
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    viewModel = sharedAuthViewModel
                )
            }
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    sharedAuthViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
