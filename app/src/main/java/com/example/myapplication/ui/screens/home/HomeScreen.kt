package com.example.myapplication.ui.screens.home

import MainProgressIndicator
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.ui.components.dialogs.SortDialog
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.SortOption
import com.example.myapplication.viewmodel.SuggestionType
import com.example.myapplication.viewmodel.TrainerCategory
import com.example.myapplication.viewmodel.TrainersViewModel
import androidx.compose.foundation.layout.FlowRow
import com.example.myapplication.ui.components.buttons.MapFloatingButton

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

    // Define consistent styling variables to match TrainerCard
    val primaryColor = Color.Black
    val borderColor = Color.DarkGray
    val backgroundColor = Color.White
    val shape = RoundedCornerShape(12.dp)

    // Obsługa przycisku "Wstecz", aby zamknąć sugestie
    BackHandler(enabled = trainersState.suggestions.isNotEmpty()) {
        trainersViewModel.clearSuggestions()
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        trainersViewModel.loadInitialTrainers()
    }

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(R.string.find_your_trainer),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Search Row with Suggestions ---
                Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) { // zIndex to ensure suggestions are on top
                    OutlinedTextField(
                        value = searchTrainerText,
                        onValueChange = { 
                            searchTrainerText = it
                            trainersViewModel.onSearchQueryChanged(it)
                            if (it.isEmpty()) {
                                trainersViewModel.applyFiltersAndLoad()
                            }
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            LazyColumn {
                                items(trainersState.suggestions) { suggestion ->
                                    ListItem(
                                        headlineContent = { Text(suggestion.title, fontWeight = FontWeight.Bold) },
                                        supportingContent = { 
                                            if (suggestion.subtitle != null) Text(suggestion.subtitle, style = MaterialTheme.typography.bodySmall) 
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = if (suggestion.type == SuggestionType.TRAINER) Icons.Default.Person else Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = primaryColor
                                            )
                                        },
                                        modifier = Modifier
                                            .clickable {
                                                searchTrainerText = suggestion.title
                                                trainersViewModel.onSuggestionClicked(suggestion)
                                                focusManager.clearFocus()
                                            }
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Filter & Sort Buttons ---
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
                        // Poprawione sprawdzanie lokalizacji
                        var isLocationAvailable = trainersState.userLocation?.let {
                            it.latitude != 0.0 && it.longitude != 0.0
                        } ?: false
                        Log.d("DIST", "Obiekt location: ${trainersState.userLocation}")
                        Log.d("DIST", "aaaaa " + isLocationAvailable.toString())
                        SortDialog(
                            onDismiss = { showSortDialog = false },
                            onSortSelected = { sortOption ->
                                trainersViewModel.onSortOptionSelected(sortOption)
                                showSortDialog = false
                            },
                            currentSortOption = trainersState.sortBy, // Przekazujemy aktualną opcję
                            isLocationAvailable = isLocationAvailable // Przekazujemy dostępność lokalizacji
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!homeState.isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing for card separation
                    ) {
                        items(trainers) { trainer ->
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
                                }
                            )
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

@Composable
fun TrainerProfileCard(
    trainer: Trainer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader


    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val imageUrl = trainer.images?.firstOrNull()
                    val isVideo = imageUrl?.contains(".mp4", ignoreCase = true) == true

                    if (imageUrl != null) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = "Zdjęcie trenera: ${trainer.firstName} ${trainer.lastName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            error = {
                                Image(
                                    painter = painterResource(id = R.drawable.placeholder),
                                    contentDescription = "Błąd ładowania",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.placeholder),
                            contentDescription = "Brak zdjęcia",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Odtwórz wideo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }


                    RatingIndicator(
                        rating = trainer.avgRating ?: "0.0", modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )


                }

                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${trainer.firstName} ${trainer.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${trainer.pricePerHour ?: 0} zł/h",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${trainer.experience ?: 0} lata doświadczenia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrainerCategory.entries
                        .forEach { categoryEnum ->
                            if (trainer.categories?.contains(categoryEnum.name) == true) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = categoryEnum.stringResId),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                }


            }
        })
}


@Composable
fun RatingIndicator(
    rating: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating star",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(size)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = rating,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun Modifier.simplePlaceholder(
    visible: Boolean,
    color: Color
): Modifier = this.then(
    Modifier.background(if (visible) color else Color.Transparent)
)
