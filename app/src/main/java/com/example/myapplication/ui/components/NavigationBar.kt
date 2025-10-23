package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R

enum class Destination(
    val route: String,
    val label: String,
    val iconRes: Int,
    val contentDescription: String
) {
    HOME("home", "Home", R.drawable.home, "Home"),
    SCHEDULER("scheduler", "Scheduler", R.drawable.scheduler, "Scheduler"),
    CHATS("chats", "Chats", R.drawable.chats, "Chats"),
    PROFILE("profile", "Profile", R.drawable.user, "Profile")
    //można dodać później TRAINERPROFILE, żeby trener miał dodatkową ikonkę
}

@Composable
fun NavigationBarExample(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val currentRoute = currentRoute(navController)

    NavigationBar(modifier = modifier.navigationBarsPadding().background(color=Color.White), windowInsets = NavigationBarDefaults.windowInsets) {
        Destination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(Destination.HOME.route)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = destination.iconRes),
                        contentDescription = destination.contentDescription,
                        modifier = modifier.height(32.dp).aspectRatio(1f).alpha(1.0f)
                    )

                },
                label = {},
                modifier = modifier.background(color = Color.White)
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

