package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(Screen.Welcome.route) {
            StartScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegistrationFlow.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegistrationFlow.route) {
                        popUpTo(Screen.Welcome.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToPersonalInfo = {
                    navController.navigate(Screen.PersonalInfo.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        navigation(
            startDestination = Screen.Register.route,
            route = Screen.RegistrationFlow.route
        ) {
            composable(Screen.Register.route) {
                val navBackStackEntry = remember(navController) {
                    navController.getBackStackEntry(Screen.RegistrationFlow.route)
                }
                val sharedAuthViewModel: AuthViewModel = hiltViewModel(navBackStackEntry)

                CredentialsRegistrationScreen(
                    onNavigateBack = {
                        sharedAuthViewModel.clearPendingGoogleRegistration()
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

            composable(Screen.PersonalInfo.route) {
                val navBackStackEntry = remember(navController) {
                    navController.getBackStackEntry(Screen.RegistrationFlow.route)
                }
                val sharedAuthViewModel: AuthViewModel = hiltViewModel(navBackStackEntry)

                PersonalInfoRegistrationScreen(
                    onNavigateBack = {
                        sharedAuthViewModel.clearPendingGoogleRegistration()
                        navController.popBackStack()
                    },
                    onRegistrationSuccess = {
                        sharedAuthViewModel.clearPendingGoogleRegistration()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    viewModel = sharedAuthViewModel
                )
            }
        }

        composable(route = Screen.Home.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            HomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
