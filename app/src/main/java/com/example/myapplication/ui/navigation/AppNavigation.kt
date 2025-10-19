package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.StartScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegistrationScreen
import com.example.myapplication.ui.screens.HomeScreen

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
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onRegistrationSuccess = { firstName, lastName, email ->
                    navController.navigate(Screen.Home.createRoute(firstName, lastName, email)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("firstName") { type = NavType.StringType },
                navArgument("lastName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val firstName = backStackEntry.arguments?.getString("firstName") ?: ""
            val lastName = backStackEntry.arguments?.getString("lastName") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""

            HomeScreen(
                firstName = firstName,
                lastName = lastName,
                email = email,
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
