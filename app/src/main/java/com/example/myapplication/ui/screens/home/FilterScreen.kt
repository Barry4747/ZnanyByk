package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodel.TrainersViewModel
import java.text.NumberFormat
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainersViewModel = hiltViewModel()
) {
    val trainersState by viewModel.trainersState.collectAsState()

    val availableCategories = remember {
        listOf("Trening siłowy", "Joga", "Pilates", "CrossFit", "Bieganie", "Sztuki walki", "Dietetyka")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtry") },
                navigationIcon = {

                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearFilters() }, // Przycisk do czyszczenia
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Wyczyść")
                }
                Button(
                    onClick = {
                        viewModel.applyFiltersAndLoad()
                        onNavigateBack() // Wróć do poprzedniego ekranu
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zastosuj")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "Cena za godzinę",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Wyświetlanie aktualnego zakresu cen
            Text(
                text = "${trainersState.priceMin} zł - ${trainersState.priceMax} zł",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            RangeSlider(
                value = trainersState.priceMin.toFloat()..trainersState.priceMax.toFloat(),
                onValueChange = { range ->
                    viewModel.onPriceRangeChanged(range.start.toInt(), range.endInclusive.toInt())
                },
                valueRange = 0f..500f, // Całkowity możliwy zakres
                steps = 49 // (500-0)/10 - 1 = 49 kroków co 10 zł
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Minimalna ocena",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%.1f ★ lub wyższa".format(trainersState.minRating),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Slider(
                value = trainersState.minRating,
                onValueChange = { viewModel.onMinRatingChanged(it) },
                valueRange = 0f..5f,
                steps = 9 // (5-0)/0.5 - 1 = 9 kroków co 0.5 gwiazdki
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SEKCJA KATEGORII ---
            Text(
                text = "Specjalizacje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Używamy FlowRow, aby "pigułki" z kategoriami ładnie się zawijały
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { category ->
                    val isSelected = category in trainersState.selectedCategories
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Zaznaczone",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}
