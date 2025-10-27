package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.ui.components.CustomBottomBar
import com.example.myapplication.ui.components.Destination
import com.example.myapplication.ui.components.currentRoute
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import com.google.android.libraries.places.api.Places
import com.example.myapplication.BuildConfig

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        val keepOn = AtomicBoolean(true)
        splash.setKeepOnScreenCondition { keepOn.get() }


        super.onCreate(savedInstanceState)

        //move up??
        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY)
        }


        lifecycleScope.launch {
            val hasCached = try {
                authRepository.updateCachedUserFromFirebaseAuth()
            } catch (e: Exception) {
                false
            }

            keepOn.set(false)
            setTheme(R.style.Theme_MyApplication)

            setContent {
                MyApplicationTheme {
                    val navController = rememberNavController()
                    Scaffold(modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val route = currentRoute(navController)
                            if (route in listOf(
                                    Destination.HOME.route,
                                    Destination.SCHEDULER.route,
                                    Destination.CHATS.route,
                                    Destination.USER.route
                                )
                            ) {
                                CustomBottomBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = if (hasCached) "home_flow" else Screen.Welcome.route
                        )
                    }
                }
            }
        }

        enableEdgeToEdge()
    }
}
