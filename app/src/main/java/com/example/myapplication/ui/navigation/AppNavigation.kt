package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.myapplication.ui.components.CustomBottomBar
import com.example.myapplication.ui.components.Destination
import com.example.myapplication.ui.screens.SplashScreen
import com.example.myapplication.ui.screens.WelcomeScreen
import com.example.myapplication.ui.screens.auth.CredentialsRegistrationScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.PersonalInfoRegistrationScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.RegistrationViewModel
import com.example.myapplication.ui.screens.scheduler.SchedulerScreen


@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
        exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
        popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToStart = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegistrationFlow.route)
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
                    navController.navigate(Screen.RegistrationFlow.route) {
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
            composable(Screen.Register.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.RegistrationFlow.route)
                }
                val sharedRegistrationViewModel: RegistrationViewModel = hiltViewModel(parentEntry)

                CredentialsRegistrationScreen(
                    onNavigateBack = {
                        sharedRegistrationViewModel.clearPendingGoogleRegistration()
                        navController.popBackStack()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPersonalInfo = {
                        navController.navigate(Screen.PersonalInfo.route)
                    },
                    onGoogleSignInSuccess = {
                        sharedRegistrationViewModel.clearPendingGoogleRegistration()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    viewModel = sharedRegistrationViewModel
                )
            }

            composable(Screen.PersonalInfo.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.RegistrationFlow.route)
                }
                val sharedRegistrationViewModel: RegistrationViewModel = hiltViewModel(parentEntry)

                PersonalInfoRegistrationScreen(
                    onNavigateBack = {
                        sharedRegistrationViewModel.clearPendingGoogleRegistration()
                        navController.popBackStack()
                    },
                    onRegistrationSuccess = {
                        sharedRegistrationViewModel.clearPendingGoogleRegistration()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    viewModel = sharedRegistrationViewModel
                )
            }
        }

        /*
        *tutaj można dodawać ekrany, które mają mieć navigation bar*/

        composable(Screen.Home.route) {
            val bottomNavController = rememberNavController()

            Scaffold(
                bottomBar = {
                    CustomBottomBar(navController = bottomNavController)
                }
            ) { innerPadding ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = Destination.HOME.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Destination.HOME.route) {
                        val authViewModel: AuthViewModel = hiltViewModel()
                        HomeScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Destination.SCHEDULER.route) {
                        SchedulerScreen()
                    }

                    composable(Destination.CHATS.route) {
                        /* ChatsScreen() */
                    }

                    composable(Destination.PROFILE.route) {
                        /* ProfileScreen() */
                    }
                }
            }
        }

    }
}


