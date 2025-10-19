package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.CredentialsRegistrationScreen
import com.example.myapplication.ui.screens.PersonalInfoRegistrationScreen
import com.example.myapplication.ui.screens.StartScreen
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate("registration_flow") {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = Screen.Register.route,
            route = "registration_flow"
        ) {
            composable(Screen.Register.route) { backStackEntry ->
                // Pobierz ViewModel z parent navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registration_flow")
                }
                val viewModel: AuthViewModel = hiltViewModel(parentEntry)

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
                    viewModel = viewModel
                )
            }

            composable(Screen.PersonalInfo.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registration_flow")
                }
                val viewModel: AuthViewModel = hiltViewModel(parentEntry)

                PersonalInfoRegistrationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
