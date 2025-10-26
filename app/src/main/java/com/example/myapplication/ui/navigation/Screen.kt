package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object PersonalInfo : Screen("personal_info")
    object Home : Screen("home")
    object Scheduler : Screen("scheduler")
    object RegisterTrainer : Screen("register_trainer")
    object RegistrationFlow : Screen("registration_flow")
    object PasswordReset : Screen("password_reset")
}
