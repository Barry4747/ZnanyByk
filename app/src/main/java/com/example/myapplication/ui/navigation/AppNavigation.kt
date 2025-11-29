package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.myapplication.ui.components.CustomBottomBar
import com.example.myapplication.ui.components.Destination
import com.example.myapplication.ui.screens.WelcomeScreen
import com.example.myapplication.ui.screens.auth.CredentialsRegistrationScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.PasswordResetScreen
import com.example.myapplication.ui.screens.auth.PersonalInfoRegistrationScreen
import com.example.myapplication.ui.screens.chats.ChatScreen
import com.example.myapplication.ui.screens.chats.ChatsListScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.home.MapScreen
import com.example.myapplication.ui.screens.scheduler.AppointmentsScreen
import com.example.myapplication.ui.screens.profile.LocationOnboardingScreen
import com.example.myapplication.ui.screens.profile.PersonalInfoEditScreen
import com.example.myapplication.ui.screens.profile.ProfileScreen
import com.example.myapplication.ui.screens.profile.TrainerEditScreen
import com.example.myapplication.ui.screens.home.FilterScreen
import com.example.myapplication.ui.screens.home.SortScreen
import com.example.myapplication.ui.screens.home.TrainerDetailScreen
import com.example.myapplication.ui.screens.profile.TrainerRegistrationScreen
import com.example.myapplication.ui.screens.scheduler.TrainerScheduleScreen
import com.example.myapplication.ui.screens.scheduler.TrainerScheduleScreen
import com.example.myapplication.viewmodel.profile.LocationOnboardingViewModel
import com.example.myapplication.viewmodel.profile.PInfoEditViewModel
import com.example.myapplication.viewmodel.profile.ProfileViewModel
import com.example.myapplication.viewmodel.registration.AuthViewModel
import com.example.myapplication.viewmodel.registration.RegistrationViewModel
import com.example.myapplication.viewmodel.TrainersViewModel
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.MapViewModel
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.example.myapplication.ui.screens.booking.BookingScreen
import com.example.myapplication.ui.screens.booking.PaymentScreen

private const val ANIMATION_DURATION = 400


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController, modifier: Modifier = Modifier, startDestination: String
) {

    SharedTransitionLayout(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        ANIMATION_DURATION
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(
                        ANIMATION_DURATION
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        ANIMATION_DURATION
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(
                        ANIMATION_DURATION
                    )
                )
            }) {

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
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
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onNavigateBack = {
                        navController.popBackStack()
                    }, onNavigateToRegister = {
                        navController.navigate(Screen.RegistrationFlow.route) {
                            popUpTo(Screen.Welcome.route)
                            launchSingleTop = true
                        }
                    }, onNavigateToPersonalInfo = {
                        navController.navigate(Screen.RegistrationFlow.route) {
                            popUpTo(Screen.Welcome.route)
                        }
                    }, onNavigateToPasswordReset = {
                        navController.navigate(Screen.PasswordReset.route)
                    }, onLoginSuccess = {
                        navController.navigate("home_flow") {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }, viewModel = authViewModel
                )
            }

            composable(Screen.PasswordReset.route) {
                val authViewModel: AuthViewModel = hiltViewModel()

                PasswordResetScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onNavigateBack = {
                        navController.popBackStack()
                    }, viewModel = authViewModel
                )
            }


            navigation(
                startDestination = Screen.Register.route, route = Screen.RegistrationFlow.route
            ) {
                composable(Screen.Register.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.RegistrationFlow.route)
                    }
                    val sharedRegistrationViewModel: RegistrationViewModel =
                        hiltViewModel(parentEntry)

                    CredentialsRegistrationScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        onNavigateBack = {
                            sharedRegistrationViewModel.clearPendingGoogleRegistration()
                            navController.popBackStack()
                        }, onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }, onNavigateToPersonalInfo = {
                            navController.navigate(Screen.PersonalInfo.route)
                        }, onGoogleSignInSuccess = {
                            sharedRegistrationViewModel.clearPendingGoogleRegistration()
                            navController.navigate("home_flow") {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }, viewModel = sharedRegistrationViewModel
                    )
                }

                composable(Screen.PersonalInfo.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.RegistrationFlow.route)
                    }
                    val sharedRegistrationViewModel: RegistrationViewModel =
                        hiltViewModel(parentEntry)

                    PersonalInfoRegistrationScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        onNavigateBack = {
                            sharedRegistrationViewModel.clearPendingGoogleRegistration()
                            navController.popBackStack()
                        }, onRegistrationSuccess = {
                            sharedRegistrationViewModel.clearPendingGoogleRegistration()
                            navController.navigate("home_flow") {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        },

                            onRegistrationSuccessProceedWithTrainer = {
                                sharedRegistrationViewModel.clearPendingGoogleRegistration()
                                navController.navigate(Screen.RegisterTrainer.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            },
                            viewModel = sharedRegistrationViewModel
                        )
                    }
                }

                navigation(
                    startDestination = Screen.Home.route,
                    route = "home_flow"
                ) {
                    composable(Screen.Home.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("home_flow")
                        }
                        val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                        val trainersViewModel:TrainersViewModel = hiltViewModel(parentEntry)



                    HomeScreen(
                        viewModel = homeViewModel,
                        trainersViewModel = trainersViewModel,
                        onMapClick = {
                            navController.navigate(Screen.Map.route)
                        },
                        onLogout = {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        goToFilter = {
                            navController.navigate(Screen.Filter.route)
                        },
                        goToTrainerProfileCard = {
                            navController.navigate(Screen.Trainer.route)
                        }

                        )
                    }

                    composable(Screen.Filter.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("home_flow")
                        }
                        val trainersViewModel: TrainersViewModel = hiltViewModel(parentEntry)


                        FilterScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            viewModel = trainersViewModel
                        )
                    }

                    composable(Screen.Sort.route) {
                        SortScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Trainer.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("home_flow")
                        }
                        val trainersViewModel: TrainersViewModel = hiltViewModel(parentEntry)


                        TrainerDetailScreen(
                            viewModel = trainersViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onBookClick = { trainerId ->
                                navController.navigate("booking/$trainerId")
                            }
                        )
                    }
                    composable(
                        route = "booking/{trainerId}",
                        arguments = listOf(navArgument("trainerId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val trainerId = backStackEntry.arguments?.getString("trainerId") ?: return@composable

                        BookingScreen(
                            trainerId = trainerId,
                            onNavigateToPayment = { tId, dateMillis, time ->
                                navController.navigate("payment/$tId/$dateMillis/$time")
                            },
                            onNavigateBack = navController::popBackStack
                        )
                    }
                    composable(
                        route = "payment/{trainerId}/{dateMillis}/{time}",
                        arguments = listOf(
                            navArgument("trainerId") { type = NavType.StringType },
                            navArgument("dateMillis") { type = NavType.LongType },
                            navArgument("time") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val trainerId = backStackEntry.arguments?.getString("trainerId") ?: return@composable
                        val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: return@composable
                        val time = backStackEntry.arguments?.getString("time") ?: return@composable

                        PaymentScreen(
                            trainerId = trainerId,
                            dateMillis = dateMillis,
                            time = time,
                            onNavigateBack = { navController.popBackStack() },
                            onPaymentSuccess = {
                                navController.navigate("home_flow") {
                                    popUpTo("home_flow") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Map.route) { backStackEntry ->
                        val mapViewModel: MapViewModel = hiltViewModel()
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("home_flow")
                        }
                        val sharedTrainersViewModel: TrainersViewModel = hiltViewModel(parentEntry)
                        MapScreen(
                            viewModel = mapViewModel, onNavigateBack = {
                                navController.popBackStack()

                            }, onNavigateToTrainer =  { trainer ->
                                sharedTrainersViewModel.selectTrainer(trainer)
                                navController.navigate(Screen.Trainer.route)
                            }

                        )
                    }
                }





            composable(Screen.RegisterTrainer.route) {
                TrainerRegistrationScreen(onNavigateBack = {
                    navController.navigate("home_flow") {
                        popUpTo("home_flow") { inclusive = true }
                    }
                }, onSubmit = {
                    navController.navigate("home_flow") {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.TrainerProfileEdit.route) {
                TrainerEditScreen(onNavigateBack = {
                    navController.navigate(Destination.USER.route) {
                        popUpTo(Destination.USER.route) { inclusive = true }
                    }
                }, onSubmit = {
                    navController.navigate(Destination.USER.route) {
                        popUpTo(Destination.USER.route) { inclusive = true }
                    }
                })
            }

            composable(Destination.SCHEDULER.route) {
                AppointmentsScreen(
                    onAppointmentChatClick = { chatId, receiverId ->
                        navController.navigate("chat/$chatId/$receiverId")
                    }
                )
            }

            composable("trainer_schedule") {
                TrainerScheduleScreen(
                    onNavigateBack = {
                        navController.navigate(Destination.USER.route) {
                            popUpTo(Destination.USER.route) { inclusive = true }
                        }
                    }
                )
            }

            composable("appointments") {
                AppointmentsScreen(
                    onAppointmentChatClick = { chatId, receiverId ->
                        navController.navigate("chat/$chatId/$receiverId")
                    }
                )
            }


            composable("chats") {
                ChatsListScreen(
                    onChatClick = { chatId, receiverId ->
                        navController.navigate("chat/$chatId/$receiverId")
                    })
            }

            composable(
                route = "chat/{chatId}/{receiverId}",
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("receiverId") { type = NavType.StringType })) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                val receiverId =
                    backStackEntry.arguments?.getString("receiverId") ?: return@composable

                ChatScreen(
                    chatId = chatId, receiverId = receiverId, onNavigateBack = {
                        navController.navigate(Destination.CHATS.route) {
                            popUpTo("home_flow") { inclusive = true }
                        }
                    })
            }


            composable(Destination.USER.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(viewModel = profileViewModel, onEditLocation = {
                    navController.navigate(Screen.LocationOnboarding.route) {
                        launchSingleTop = true
                    }
                }, onEditProfile = {
                    navController.navigate(Screen.PersonalInfoEdit.route) {
                        launchSingleTop = true
                    }
                }, onEditTrainerProfile = {
                    navController.navigate(Screen.TrainerProfileEdit.route) {
                        launchSingleTop = true
                    }
                }, onBecomeTrainer = {
                    navController.navigate(Screen.RegisterTrainer.route) {
                        launchSingleTop = true
                    }
                },
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                }, //TODO mati zobacz se to
                        onEditSchedule = {
                        navController.navigate("trainer_schedule") {
                            launchSingleTop = true

                        }
                    }
                    )
            }

            composable(Screen.LocationOnboarding.route) {
                val locationOnboardingViewModel: LocationOnboardingViewModel = hiltViewModel()
                LocationOnboardingScreen(viewModel = locationOnboardingViewModel, onNavigateBack = {
                    navController.navigate(Destination.USER.route) {
                        popUpTo(Destination.USER.route) { inclusive = true }
                    }
                }, onNavigateToDashboard = {
                    navController.navigate("home_flow") {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.PersonalInfoEdit.route) {
                val pInfoEditViewModel: PInfoEditViewModel = hiltViewModel()
                PersonalInfoEditScreen(viewModel = pInfoEditViewModel, onNavigateBack = {
                    navController.navigate(Destination.USER.route) {
                        popUpTo(Destination.USER.route) { inclusive = true }
                    }
                }, onEditSuccess = {
                    navController.navigate(Destination.USER.route) {
                        popUpTo(Destination.USER.route) { inclusive = true }
                    }
                })
            }
        }
    }
}
