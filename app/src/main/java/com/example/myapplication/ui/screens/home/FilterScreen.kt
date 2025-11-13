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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.values
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.viewmodel.TrainerCategory
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

    val availableCategories = remember { TrainerCategory.entries.toTypedArray() }

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
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Wyczyść")
                }
                Button(
                    onClick = {
                        viewModel.applyFiltersAndLoad()
                        onNavigateBack()
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
                valueRange = 0f..10000f,
                steps = 49
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
                        }
                    )
                }
            }
        }
    }
}
