package com.example.myapplication.ui.screens.profile

import MainProgressIndicator
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.myapplication.R
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.users.TrainerCategory
import com.example.myapplication.ui.components.buttons.FormButton
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.buttons.RemoveButton
import com.example.myapplication.ui.components.chips.MainCategoryChip
import com.example.myapplication.ui.components.dialogs.CategorySelectionDialog
import com.example.myapplication.ui.components.dialogs.GymSelectionDialog
import com.example.myapplication.ui.components.fields.MainFormTextField
import com.example.myapplication.viewmodel.trainer.TrainerRegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerRegistrationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    viewModel: TrainerRegistrationViewModel = hiltViewModel(),
) {
    var hourlyRate by remember { mutableStateOf("") }
    var selectedGym by remember { mutableStateOf<Gym?>(null) }
    var description by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<TrainerCategory>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var showGymDialog by remember { mutableStateOf(false) }
    var showMediaPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) viewModel.uploadMedias(context, uris)
        showMediaPicker = false
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) viewModel.uploadMedias(context, uris)
        showMediaPicker = false
    }

    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            MainBackButton(
                onClick = onNavigateBack
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = state.userName.ifBlank { stringResource(R.string.placeholder_username) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.fillout_trainer_profile),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            MainFormTextField(
                value = hourlyRate,
                onValueChange = { input -> hourlyRate = input.filter { it.isDigit() } },
                label = stringResource(R.string.hourly_rate),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormButton(
                text = selectedGym?.gymName ?: stringResource(R.string.my_gym),
                onClick = { showGymDialog = true },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description_label),
                enabled = !state.isLoading,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MainFormTextField(
                value = experienceYears,
                onValueChange = { input -> experienceYears = input.filter { it.isDigit() } },
                label = stringResource(R.string.how_long_trainer),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.select_images_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )
            Box(modifier = Modifier.height(140.dp)) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.height(80.dp)) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(state.selectedImages) { uri ->
                                Box(modifier = Modifier.size(76.dp)) {
                                    val isVideo = viewModel.isVideoUri(context, uri)

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(uri)
                                            .apply {
                                                if (isVideo) {
                                                    videoFrameMillis(1000)
                                                }
                                            }
                                            .crossfade(true)
                                            .size(76)
                                            .build(),
                                        contentDescription = stringResource(R.string.trainer_upload_preview),
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isVideo) {
                                        Icon(
                                            painter = painterResource(R.drawable.play_icon),
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center),
                                            tint = Color.White
                                        )
                                    }

                                    RemoveButton(
                                        onClick = { viewModel.removeSelectedMedia(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                            items(state.uploadedImages) { url ->
                                Box(modifier = Modifier.size(76.dp)) {
                                    val isVideo = viewModel.isVideoUrl(url)

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(url)
                                            .crossfade(true)
                                            .size(76)
                                            .build(),
                                        contentDescription = stringResource(R.string.trainer_upload_preview),
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isVideo) {
                                        Icon(
                                            painter = painterResource(R.drawable.play_icon),
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center),
                                            tint = Color.White
                                        )
                                    }

                                    RemoveButton(
                                        onClick = { viewModel.removeUploadedMedia(url) },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                            item {
                                MainCategoryChip(label = stringResource(R.string.add_button_plus), onClick = { showMediaPicker = true })
                            }
                        }
                    }
                }
            }

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
                        MainCategoryChip(label = stringResource(R.string.add_button_plus), onClick = { showDialog = true })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading || state.isUploadingImages) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MainProgressIndicator()
                    if (state.isUploadingImages) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.uploading_images),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                MainButton(
                    text = stringResource(R.string.confirm_trainer_profile_creation),
                    onClick = {
                        viewModel.submitTrainerProfile(
                            hourlyRate = hourlyRate,
                            gymId = selectedGym?.id,
                            description = description,
                            experienceYears = experienceYears,
                            selectedCategories = selectedCategories.map { it.name },
                            images = state.uploadedImages
                        )
                    },
                    enabled = hourlyRate.isNotBlank() && description.isNotBlank() && experienceYears.isNotBlank() && !state.isUploadingImages,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = Color.Red
                )
            }

            if (state.successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.successMessage ?: "",
                    color = Color.Green
                )
            }
        }
    }

    LaunchedEffect(key1 = state.successMessage) {
        if (state.successMessage != null) {
            onSubmit()
        }
    }

    if (showGymDialog) {
        GymSelectionDialog(
            gyms = state.gyms,
            onDismiss = { showGymDialog = false },
            onGymSelected = { gym ->
                selectedGym = gym
                showGymDialog = false
            }
        )
    }

    if (showDialog) {
        CategorySelectionDialog(
            selectedCategories = selectedCategories,
            onDismiss = { showDialog = false },
            onCategoryClick = { category ->
                selectedCategories = if (category in selectedCategories) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
            }
        )
    }

    if (showMediaPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMediaPicker = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_media_type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                MainButton(
                    text = stringResource(R.string.select_images),
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )

                MainButton(
                    text = stringResource(R.string.select_videos),
                    onClick = { videoLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainerRegistrationScreenPreview() {
    TrainerRegistrationScreen()
}
