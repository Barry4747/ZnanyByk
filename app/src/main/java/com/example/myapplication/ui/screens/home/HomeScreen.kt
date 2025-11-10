package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.values
import androidx.hilt.navigation.compose.hiltViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.SortOption
import com.example.myapplication.viewmodel.TrainersViewModel
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    goToFilter: () -> Unit,
    goToTrainerProfileCard: (Trainer) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    trainersViewModel: TrainersViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val trainersState by trainersViewModel.trainersState.collectAsState()


    val user = homeState.user
    val trainers: List<Trainer> = trainersState.trainers
    val errorMessage = homeState.errorMessage
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    var showSortDialog by remember { mutableStateOf(false) }
    var searchTrainerText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        trainersViewModel.loadInitialTrainers()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.find_your_trainer),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = searchTrainerText,
            onValueChange = { searchTrainerText = it},
            label = { Text(stringResource(R.string.search)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_icon)
            )
        }, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    trainersViewModel.searchTrainers(searchTrainerText)
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {goToFilter()},
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtruj",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Filtruj")
            }
            OutlinedButton(
                onClick = { showSortDialog=true
                          Log.d("Filtry", trainersState.toString())},
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sortuj",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Sortuj")
            }
            if (showSortDialog) {
                SortDialog(
                    onDismiss = { showSortDialog = false }, // Zamknij dialog
                    onSortSelected = { sortOption ->
                        trainersViewModel.onSortOptionSelected(sortOption)
                        showSortDialog = false // Zamknij dialog po wyborze
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center // Wycentruje wskaźnik ładowania
        ) {
            // LazyColumn jest zawsze w Box, ale jest widoczny tylko gdy nie ma ładowania
            if (!homeState.isLoading) {
                LazyColumn(
                    // Nie potrzebuje już modyfikatora weight, bo jest w Box, który go ma
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trainers) { trainer ->
                        TrainerProfileCard(trainer = trainer, onClick = {
                            if (trainer.images?.isNotEmpty() == true) {
                                val imageUrl = trainer.images[0]
                                // 2. Zbuduj zapytanie do Coila, aby "podgrzać" cache
                                val request = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    // Opcjonalnie: nie musisz czekać na wynik, robisz to w tle
                                    .build()
                                // 3. Zleć Coilowi wykonanie zapytania w tle
                                imageLoader.enqueue(request)
                            }
                            trainersViewModel.selectTrainer(trainer)
                            goToTrainerProfileCard(trainer)})
                    }
                }
            }

            if (homeState.isLoading) {
                CircularProgressIndicator()
            }
        }

        if (errorMessage != null && !homeState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
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
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.logout))
        }
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

    LaunchedEffect(key1 = trainer) {

        delay(250)

        trainer.images?.forEach { imageUrl ->
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
            imageLoader.enqueue(request) // Zleć ładowanie do cache'u w tle
        }
    }
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
                Image(
                    painter = painterResource(id = R.drawable.gym_trainer_example),
                    contentDescription = "Zdjęcie trenera: ${trainer.firstName} ${trainer.lastName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                    RatingIndicator(rating=trainer.avgRating ?: "0.0", modifier = Modifier
                            .align(Alignment.TopEnd) // Przyklej do prawego górnego rogu Box
                            .padding(8.dp) // Odsuń lekko od krawędzi
                            .background(
                                color = MaterialTheme.colorScheme.surface, // Biały kolor tła
                                shape = RoundedCornerShape(16.dp) // Zaokrąglone rogi
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)) // Wewnętrzny padding etykiety


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
                        color = MaterialTheme.colorScheme.tertiary)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Odstęp poziomy między pigułkami
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Odstęp pionowy, gdy pigułki się zawiną
                ) {
                    trainer.categories?.forEach { speciality ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer, // Szary kolor tła
                                    shape = RoundedCornerShape(12.dp) // Zaokrąglone rogi
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp) // Wewnętrzny padding
                        ) {
                            Text(
                                text = speciality,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // --- KONIEC NOWEJ SEKCJI ---

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}

@Composable
fun SortDialog(
    onDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sortuj według") },
        text = {
            Column {
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
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

