package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.Trainer
import com.example.myapplication.viewmodel.HomeViewModel


@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val user = homeState.user
    val trainers = homeState.trainers
    val errorMessage = homeState.errorMessage

    var searchTrainerText by remember { mutableStateOf("") }

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
        }, singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f), // weight(1f) sprawia, że lista zajmie całą dostępną przestrzeń
            verticalArrangement = Arrangement.spacedBy(12.dp) // Odstęp między elementami
        ) {
            items(trainers) { trainer ->
                TrainerProfileCard(trainer = trainer, onClick = { /* TODO: Obsłuż kliknięcie */ })
            }
        }

        if (homeState.isLoading) {
            CircularProgressIndicator()
        }  else if (errorMessage != null) {
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
    Log.d("TRAINER_OBJECT", "Trener: ${trainer}")

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
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Przyklej do prawego górnego rogu Box
                            .padding(8.dp) // Odsuń lekko od krawędzi
                            .background(
                                color = MaterialTheme.colorScheme.surface, // Biały kolor tła
                                shape = RoundedCornerShape(16.dp) // Zaokrąglone rogi
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp), // Wewnętrzny padding etykiety
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star, // Ikona gwiazdki
                            contentDescription = "Rating star",
                            tint = Color(0xFFFFC107), // Żółty/złoty kolor gwiazdki
                            modifier = Modifier.size(16.dp) // Mały rozmiar ikonki
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trainer.avgRating ?: "0.00",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface // Kolor tekstu pasujący do tła
                        )
                    }
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
            }
        }
    )
}

@Composable
fun UserDataRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
