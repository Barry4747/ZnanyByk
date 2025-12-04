package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R

enum class Destination(
    val route: String,
    val label: String,
    val iconRes: Int,
    val iconActiveRes: Int,
    val contentDescription: String
) {
    HOME("home", "Home", R.drawable.home, R.drawable.home_active, "Home"),
    CHATS("chats", "Chats", R.drawable.chats, R.drawable.chats_active, "Chats"),
    SCHEDULER("scheduler", "Scheduler", R.drawable.scheduler, R.drawable.scheduler_active, "Scheduler"),
    USER("profile", "Profile", R.drawable.user, R.drawable.user_active, "Profile")
    //można dodać później TRAINERPROFILE, żeby trener miał dodatkową ikonkę
}



@Composable
fun CustomBottomBar(
    navController: NavHostController,
    height: Dp = 72.dp,
    iconSize: Dp = 24.dp
) {
    val currentRoute = currentRoute(navController)

    // Używamy Column jako kontenera na tło i cień
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, spotColor = Color.Black.copy(alpha = 0.2f))
            .background(Color.White)
        // WAŻNE: Nie dodajemy tu windowInsetsPadding, bo zrobimy to Spacerem na dole
    ) {
        // 1. Właściwa zawartość paska (ikony) - ma sztywne 72dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            verticalAlignment = Alignment.CenterVertically // Dodane dla pewności
        ) {
            Destination.entries.forEach { destination ->
                val isSelected = currentRoute == destination.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(Destination.HOME.route)
                                    launchSingleTop = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) destination.iconActiveRes else destination.iconRes
                        ),
                        contentDescription = destination.contentDescription,
                        modifier = Modifier.size(iconSize),
                        // Opcjonalnie: kolorowanie ikon, jeśli nie są pre-kolorowane w drawable
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // 2. Magiczny Spacer, który "wypycha" tło pod pasek systemowy
        // Na Androidzie z gestami będzie miał ~0-10dp, na Androidzie z przyciskami ~48dp.
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}