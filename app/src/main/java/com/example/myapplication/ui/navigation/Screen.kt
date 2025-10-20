package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object PersonalInfo : Screen("personal_info")
    object Home : Screen("home")

    object RegistrationFlow : Screen("registration_flow")
}
