package com.example.myapplication.ui.screens.profile

import MainProgressIndicator
import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.TrainerCategory
import com.example.myapplication.ui.components.MainTopBar
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.RemoveButton
import com.example.myapplication.ui.components.chips.MainCategoryChip
import com.example.myapplication.ui.components.fields.MainFormTextField

@Composable
fun TrainerForm(
    modifier: Modifier = Modifier,
    userName: String,
    titleRes: Int,
    hourlyRate: String,
    onHourlyRateChange: (String) -> Unit,
    selectedGymName: String?,
    onGymClick: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    experienceYears: String,
    onExperienceChange: (String) -> Unit,
    selectedCategories: List<TrainerCategory>,
    onCategoryDialogOpen: () -> Unit,
    existingImages: List<String> = emptyList(),
    uploadedImages: List<String> = emptyList(),
    selectedImages: List<Uri> = emptyList(),
    isVideoUri: (Uri) -> Boolean = { false },
    isVideoUrl: (String) -> Boolean = { false },
    onRemoveExistingImage: (String) -> Unit = {},
    onRemoveUploadedImage: (String) -> Unit = {},
    onRemoveSelectedImage: (Uri) -> Unit = {},
    onMediaPickerOpen: () -> Unit = {},
    isLoading: Boolean,
    isUploadingImages: Boolean,
    onSubmit: () -> Unit,
    submitTextRes: Int,
    submitEnabled: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainTopBar(
                onNavigateBack = onNavigateBack,
                text = userName.ifBlank { stringResource(R.string.placeholder_username) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- HOURLY RATE WITH FIXED PLN SUFFIX ---
            MainFormTextField(
                value = hourlyRate,
                onValueChange = onHourlyRateChange,
                label = stringResource(R.string.hourly_rate),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = "PLN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- GYM SELECTOR (LOOKS LIKE DROPDOWN) ---
            // FIXED: Używamy Boxa z nakładką (overlay), aby przechwycić kliknięcie
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                MainFormTextField(
                    value = selectedGymName ?: stringResource(R.string.my_gym),
                    onValueChange = {}, // Ignorowane, bo readOnly
                    label = stringResource(R.string.my_gym),
                    enabled = true, // Pozostawiamy true dla estetyki (nie wyszarzone)
                    readOnly = true, // Klawiatura się nie otworzy
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Gym"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Niewidoczna nakładka, która przechwytuje kliknięcie
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = !isLoading) { onGymClick() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.description_label),
                enabled = !isLoading,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = experienceYears,
                onValueChange = onExperienceChange,
                label = stringResource(R.string.how_long_trainer),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.select_images_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            // --- MEDIA PREVIEWS WITH STYLING ---
            Box(modifier = Modifier.height(140.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.height(80.dp)) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val mediaShape = RoundedCornerShape(12.dp)
                            val mediaModifier = Modifier
                                .size(80.dp)
                                .clip(mediaShape)
                                .border(1.dp, Color.LightGray, mediaShape)

                            items(existingImages) { url ->
                                Box(modifier = mediaModifier) {
                                    val isVideo = isVideoUrl(url)

                                    if (!isVideo) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(url)
                                                .crossfade(true)
                                                .size(200)
                                                .build(),
                                            contentDescription = stringResource(R.string.trainer_upload_preview),
                                            placeholder = painterResource(R.drawable.image_placeholder),
                                            error = painterResource(R.drawable.image_placeholder),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (isVideo) {
                                        Icon(
                                            painter = painterResource(R.drawable.play_icon),
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .align(Alignment.Center),
                                            tint = Color.Black
                                        )
                                    }

                                    RemoveButton(
                                        onClick = { onRemoveExistingImage(url) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    )
                                }
                            }

                            items(uploadedImages) { url ->
                                Box(modifier = mediaModifier) {
                                    val isVideo = isVideoUrl(url)

                                    if (!isVideo) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(url)
                                                .crossfade(true)
                                                .size(200)
                                                .build(),
                                            contentDescription = stringResource(R.string.trainer_upload_preview),
                                            placeholder = painterResource(R.drawable.image_placeholder),
                                            error = painterResource(R.drawable.image_placeholder),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (isVideo) {
                                        Icon(
                                            painter = painterResource(R.drawable.play_icon),
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .align(Alignment.Center),
                                            tint = Color.Black
                                        )
                                    }

                                    RemoveButton(
                                        onClick = { onRemoveUploadedImage(url) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    )
                                }
                            }

                            items(selectedImages) { uri ->
                                Box(modifier = mediaModifier) {
                                    val isVideo = isVideoUri(uri)

                                    if (!isVideo) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(uri)
                                                .crossfade(true)
                                                .size(200)
                                                .build(),
                                            contentDescription = stringResource(R.string.trainer_upload_preview),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (isVideo) {
                                        Icon(
                                            painter = painterResource(R.drawable.play_icon),
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .align(Alignment.Center),
                                            tint = Color.Black
                                        )
                                    }

                                    RemoveButton(
                                        onClick = { onRemoveSelectedImage(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    )
                                }
                            }

                            item {
                                MainCategoryChip(
                                    label = stringResource(R.string.add_button_plus),
                                    onClick = onMediaPickerOpen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.select_categories_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            Box(modifier = Modifier.height(56.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(selectedCategories) { category ->
                        MainCategoryChip(category = category)
                    }
                    item {
                        MainCategoryChip(
                            label = stringResource(R.string.add_button_plus),
                            onClick = onCategoryDialogOpen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading || isUploadingImages) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MainProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isUploadingImages)
                            stringResource(R.string.uploading_images)
                        else
                            stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                MainButton(
                    text = stringResource(submitTextRes),
                    onClick = onSubmit,
                    enabled = submitEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}