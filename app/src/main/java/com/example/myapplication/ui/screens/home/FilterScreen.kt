package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.AlternateButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.viewmodel.TrainerCategory
import com.example.myapplication.viewmodel.TrainersViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainersViewModel = hiltViewModel()
) {
    val trainersState by viewModel.trainersState.collectAsState()

    val availableCategories = remember { TrainerCategory.entries.toTypedArray() }

    var minPriceText by remember { mutableStateOf(trainersState.priceMin.toString()) }
    var maxPriceText by remember { mutableStateOf(trainersState.priceMax.toString()) }

    // Synchronize TextField state with ViewModel state
    LaunchedEffect(trainersState.priceMin) {
        minPriceText = trainersState.priceMin.toString()
    }
    LaunchedEffect(trainersState.priceMax) {
        maxPriceText = trainersState.priceMax.toString()
    }

    Scaffold(
        topBar = {
            MainTopBar(
                onNavigateBack = onNavigateBack,
                text = "Filtry"
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AlternateButton(
                    text = "Wyczyść",
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.weight(1f)
                )
                MainButton(
                    text = "Zastosuj",
                    onClick = {
                        viewModel.applyFiltersAndLoad()
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                )
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
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = {
                        minPriceText = it
                        // Update ViewModel only with valid numbers
                        it.toIntOrNull()?.let { newMin ->
                            if (newMin <= trainersState.priceMax) {
                                viewModel.onPriceRangeChanged(newMin, trainersState.priceMax)
                            }
                        }
                    },
                    label = { Text("Od") },
                    suffix = { Text("zł") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = {
                        maxPriceText = it
                        // Update ViewModel only with valid numbers
                        it.toIntOrNull()?.let { newMax ->
                            if (newMax >= trainersState.priceMin) {
                                viewModel.onPriceRangeChanged(trainersState.priceMin, newMax)
                            }
                        }
                    },
                    label = { Text("Do") },
                    suffix = { Text("zł") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))



            RangeSlider(
                value = trainersState.priceMin.toFloat()..trainersState.priceMax.toFloat(),
                onValueChange = { range ->
                    viewModel.onPriceRangeChanged(range.start.toInt(), range.endInclusive.toInt())
                },
                valueRange = 0f..trainersState.maxPriceFromTrainers.toFloat(),
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Black,
                    inactiveTrackColor = Color.LightGray
                )
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
                steps = 9, // (5-0)/0.5 - 1 = 9 kroków co 0.5 gwiazdki
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Black,
                    inactiveTrackColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Specjalizacje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { categoryEnum ->
                    val displayName = stringResource(id=categoryEnum.stringResId)
                    val logicalKey = categoryEnum.name
                    val isSelected = logicalKey in trainersState.selectedCategories
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(logicalKey) },
                        label = { Text(displayName) },
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
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}
