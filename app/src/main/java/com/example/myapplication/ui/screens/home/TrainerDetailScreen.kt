package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.viewmodel.TrainersViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrainerDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrainersViewModel // Odbiera WSPÓŁDZIELONY ViewModel
) {
    val trainersState by viewModel.trainersState.collectAsState()
    val selectedTrainer = trainersState.selectedTrainer

    // --- GŁÓWNA ZMIANA: Używamy Box jako kontenera całego ekranu ---
    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTrainer != null) {
            // --- Główna, przewijalna treść (jest na samym spodzie) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // SEKCJA OBRAZKA
                val imageUrl = if (selectedTrainer.images?.isNotEmpty() == true) {
                    selectedTrainer.images[0]
                } else {
                    null
                }
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        placeholder = painterResource(id = R.drawable.gym_trainer_example),
                        error = painterResource(id = R.drawable.gym_trainer_example)
                    ),
                    contentDescription = "Zdjęcie trenera: ${selectedTrainer.firstName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                    contentScale = ContentScale.Crop
                )

                // SEKCJA Z INFORMACJAMI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // ... (Cała zawartość: Row z imieniem, Text z doświadczeniem, FlowRow, Opis)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedTrainer.firstName} ${selectedTrainer.lastName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedTrainer.pricePerHour ?: 0} zł/h",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "${selectedTrainer.experience ?: 0} lat doświadczenia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "${selectedTrainer.location ?: "-"} km od ciebie     ${selectedTrainer.ratings?.size ?: 0} ocen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTrainer.categories?.forEach { speciality ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "O mnie",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedTrainer.description ?: "Brak opisu.",
                        style = MaterialTheme.typography.bodyLarge
                    )


                    val galleryImages = selectedTrainer.images?.drop(1) ?: emptyList()
                    if (galleryImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Galeria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- POPRAWIONA SIATKA ---
                        // Obliczamy wysokość siatki na podstawie liczby wierszy
                        val rowCount = (galleryImages.size + 1) / 2 // +1 aby zaokrąglić w górę
                        val gridHeight = (rowCount * 150).dp // Zakładając, że każdy element ma ok. 150dp wysokości

                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight), // Nadajemy siatce stałą wysokość
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            // Wyłączamy przewijanie siatki, bo główna kolumna już się przewija
                            userScrollEnabled = false
                        ) {
                            items(galleryImages) { imageUrl ->
                                GalleryImage(
                                    imageUrl = imageUrl,
                                    onClick = { /* TODO: Otwórz obrazek w pełnym ekranie */ }
                                )
                            }
                        }
                    }

                    // Dodaj pustą przestrzeń na dole, aby ostatni tekst nie chował się pod przyciskiem
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            // Sytuacja awaryjna
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nie udało się załadować danych trenera. Wróć i spróbuj ponownie.")
            }
        }

        // --- Elementy "pływające" na wierzchu ---

        // Przycisk powrotu (bez zmian)
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 24.dp)
                .size(32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Wróć",
                tint = Color.White
            )
        }

        // Wskaźnik oceny (bez zmian)
        if (selectedTrainer != null) {
            RatingIndicator(
                rating = selectedTrainer.avgRating ?: "0.0",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Przycisk "Umów wizytę" - teraz jako element "pływający" na dole
        Button(
            onClick = { /* TODO: Logika umówienia wizyty */ },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Przyklej do dołu Boxa
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Umów wizytę")
        }
    }
}

/**
 * Reużywalny komponent do wyświetlania oceny z gwiazdką.
 * (Ta funkcja jest już w Twoim pliku, więc nie trzeba jej zmieniać)
 */
@Composable
fun RatingIndicator(
    rating: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating star",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(16.dp)
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


@Composable
fun GalleryImage(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Zdjęcie z galerii trenera",
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop, // Crop zapewnia, że obrazek wypełni kafelek bez zniekształceń
        placeholder = painterResource(id = R.drawable.gym_trainer_example),
        error = painterResource(id = R.drawable.gym_trainer_example)
    )
}
