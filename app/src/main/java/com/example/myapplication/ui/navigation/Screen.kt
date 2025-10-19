package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{firstName}/{lastName}/{email}") {
        fun createRoute(firstName: String, lastName: String, email: String): String {
            return "home/$firstName/$lastName/$email"
        }
    }
}
