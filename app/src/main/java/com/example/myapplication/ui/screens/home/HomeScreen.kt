package com.example.myapplication.ui.screens.home

import MainProgressIndicator
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.ui.components.TrainerProfileCard
import com.example.myapplication.ui.components.dialogs.SortDialog
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.TrainersViewModel

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
    var searchTrainerText by remember { mutableStateOf("") }

    // Define consistent styling variables to match TrainerCard
    val primaryColor = Color.Black
    val borderColor = Color.DarkGray
    val backgroundColor = Color.White
    val shape = RoundedCornerShape(12.dp)

    LaunchedEffect(Unit) {
        trainersViewModel.loadInitialTrainers()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor) // Ensure clean white background
            .padding(16.dp), // Increased padding slightly for better breathing room
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.find_your_trainer),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Search Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Styled Map Button (Box matching border/shape of Text Field)
            Box(
                modifier = Modifier
                    .size(56.dp) // Height matches default OutlinedTextField height
                    .border(1.dp, borderColor, shape)
                    .clip(shape)
                    .clickable { onMapClick() }
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.map_search_icon),
                    contentDescription = "Search on map",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Styled Search Field
            OutlinedTextField(
                value = searchTrainerText,
                onValueChange = { searchTrainerText = it },
                label = { Text(stringResource(R.string.search)) },
                modifier = Modifier.weight(1f),
                shape = shape, // 12.dp
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
                    }
                )
            )
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
                SortDialog(
                    onDismiss = { showSortDialog = false },
                    onSortSelected = { sortOption ->
                        trainersViewModel.onSortOptionSelected(sortOption)
                        showSortDialog = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.weight(1f),
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
}