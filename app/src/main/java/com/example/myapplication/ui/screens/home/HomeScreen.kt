package com.example.myapplication.ui.screens.home

import MainProgressIndicator
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.ui.components.TrainerProfileCard
import com.example.myapplication.ui.components.buttons.MapFloatingButton
import com.example.myapplication.ui.components.dialogs.SortDialog
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.trainer.SuggestionType
import com.example.myapplication.viewmodel.trainer.TrainersViewModel

@Composable
fun HomeScreen(
    onMapClick: () -> Unit,
    goToFilter: () -> Unit,
    goToTrainerProfileCard: (Trainer) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    trainersViewModel: TrainersViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val trainersState by trainersViewModel.trainersState.collectAsState()

    val trainers: List<Trainer> = trainersState.trainers
    val errorMessage = homeState.errorMessage
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    var showSortDialog by remember { mutableStateOf(false) }
    var searchTrainerText by remember { mutableStateOf(trainersState.searchQuery) }
    val focusManager = LocalFocusManager.current

    val primaryColor = Color.Black
    val borderColor = Color.DarkGray
    val backgroundColor = Color.White
    val shape = RoundedCornerShape(12.dp)

    BackHandler(enabled = trainersState.suggestions.isNotEmpty()) {
        trainersViewModel.clearSuggestions()
        focusManager.clearFocus()
    }


    LaunchedEffect(Unit) {
        trainersViewModel.loadInitialTrainers()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.find_your_trainer),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
                OutlinedTextField(
                    value = searchTrainerText,
                    onValueChange = { 
                        searchTrainerText = it
                        trainersViewModel.onSearchQueryChanged(it)
                    },
                    label = { Text(stringResource(R.string.search)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = primaryColor,
                        focusedLeadingIconColor = primaryColor,
                        unfocusedLeadingIconColor = Color.Gray,
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_icon)
                        )
                    },
                    trailingIcon = {
                        if (searchTrainerText.isNotEmpty()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Wyczyść wyszukiwanie",
                                modifier = Modifier.clickable {
                                    searchTrainerText = ""
                                    trainersViewModel.onSearchQueryChanged("")
                                    trainersViewModel.applyFiltersAndLoad()
                                }
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            trainersViewModel.searchTrainers(searchTrainerText)
                            trainersViewModel.clearSuggestions()
                            focusManager.clearFocus()
                        }
                    )
                )

                if (trainersState.suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .padding(top = 60.dp)
                            .fillMaxWidth()
                            .heightIn(max = 250.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = shape,
                        border = BorderStroke(1.dp, borderColor),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        LazyColumn {
                            items(trainersState.suggestions) { suggestion ->
                                ListItem(
                                    headlineContent = { Text(suggestion.title, fontWeight = FontWeight.Bold, color = primaryColor) },
                                    supportingContent = { 
                                        suggestion.subtitle?.let { 
                                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        } 
                                    },
                                    leadingContent = {
                                        Icon(
                                            imageVector = if (suggestion.type == SuggestionType.TRAINER) Icons.Default.Person else Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = primaryColor
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier
                                        .clickable {
                                            searchTrainerText = suggestion.title
                                            trainersViewModel.onSuggestionClicked(suggestion)
                                            focusManager.clearFocus()
                                        }
                                        .fillMaxWidth()
                                )
                                HorizontalDivider(color = borderColor.copy(alpha = 0.2f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { goToFilter() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = shape,
                    border = BorderStroke(1.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor,
                        containerColor = backgroundColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtruj",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtruj",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        showSortDialog = true
                        Log.d("Filtry", trainersState.toString())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = shape,
                    border = BorderStroke(1.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor,
                        containerColor = backgroundColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sortuj",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sortuj",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showSortDialog) {
                    val isLocationAvailable = trainersState.userLocation?.let {
                        it.latitude != 0.0 && it.longitude != 0.0
                    } ?: false
                    SortDialog(
                        onDismiss = { showSortDialog = false },
                        onSortSelected = { sortOption ->
                            trainersViewModel.onSortOptionSelected(sortOption)
                            showSortDialog = false
                        },
                        currentSortOption = trainersState.sortBy,
                        isLocationAvailable = isLocationAvailable
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = borderColor
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!homeState.isLoading) {
                    if (trainers.isEmpty() && !trainersState.firstLoad) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nie znaleziono trenerów",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Spróbuj zmienić filtry lub wyszukiwanie",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(trainers) { trainer ->
                                val userLocation = trainersState.userLocation
                                val gym = trainersViewModel.getGymOfTrainer(trainer)
                                val distance = if (userLocation != null && gym != null) {
                                    trainersViewModel.calculateDistanceInKm(
                                        lat1 = userLocation.latitude,
                                        lon1 = userLocation.longitude,
                                        lat2 = gym.gymLocation.latitude,
                                        lon2 = gym.gymLocation.longitude
                                    )
                                } else {
                                    null
                                }

                                TrainerProfileCard(
                                    trainer = trainer,
                                    onClick = {
                                        if (trainer.images?.isNotEmpty() == true) {
                                            val imageUrl = trainer.images[0]
                                            val request = ImageRequest.Builder(context)
                                                .data(imageUrl)
                                                .build()
                                            imageLoader.enqueue(request)
                                        }
                                        trainersViewModel.selectTrainer(trainer)
                                        goToTrainerProfileCard(trainer)
                                    },
                                    distance = distance
                                )
                            }
                        }
                    }
                }

                if (homeState.isLoading) {
                    MainProgressIndicator()
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.error),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        MapFloatingButton(
            onClick = onMapClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}
