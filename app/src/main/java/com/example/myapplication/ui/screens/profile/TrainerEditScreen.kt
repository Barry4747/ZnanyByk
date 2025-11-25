package com.example.myapplication.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.users.TrainerCategory
import com.example.myapplication.ui.components.bottomsheets.MediaPickerBottomSheet
import com.example.myapplication.ui.components.dialogs.CategorySelectionDialog
import com.example.myapplication.ui.components.dialogs.GymSelectionDialog
import com.example.myapplication.viewmodel.trainer.TrainerEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerEditScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    viewModel: TrainerEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var hourlyRate by remember { mutableStateOf("") }
    var selectedGymLocal by remember { mutableStateOf<Gym?>(null) }
    var description by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<TrainerCategory>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var showGymDialog by remember { mutableStateOf(false) }
    var showMediaPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.hourlyRate) {
        hourlyRate = state.hourlyRate
    }
    LaunchedEffect(state.selectedGym) {
        selectedGymLocal = state.selectedGym
    }
    LaunchedEffect(state.description) {
        description = state.description
    }
    LaunchedEffect(state.experienceYears) {
        experienceYears = state.experienceYears
    }
    LaunchedEffect(state.selectedCategories) {
        selectedCategories = state.selectedCategories
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { success ->
            snackbarHostState.showSnackbar(
                message = success,
                withDismissAction = true
            )
            onSubmit()
        }
    }

    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) viewModel.uploadMedias(context, uris)
        showMediaPicker = false
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) viewModel.uploadMedias(context, uris)
        showMediaPicker = false
    }

    Box {
        TrainerForm(
            modifier = modifier,
            userName = state.userName,
            titleRes = R.string.edit_trainer_profile,
            hourlyRate = hourlyRate,
            onHourlyRateChange = { input -> hourlyRate = input.filter { it.isDigit() } },
            selectedGymName = selectedGymLocal?.gymName,
            onGymClick = { showGymDialog = true },
            description = description,
            onDescriptionChange = { description = it },
            experienceYears = experienceYears,
            onExperienceChange = { input -> experienceYears = input.filter { it.isDigit() } },
            selectedCategories = selectedCategories,
            onCategoryDialogOpen = { showDialog = true },
            existingImages = state.existingImages,
            uploadedImages = state.uploadedImages,
            selectedImages = state.selectedImages,
            isVideoUri = { uri -> viewModel.isVideoUri(context, uri) },
            isVideoUrl = { url -> viewModel.isVideoUrl(url) },
            onRemoveExistingImage = { viewModel.removeImage(it) },
            onRemoveUploadedImage = { viewModel.removeUploadedImage(it) },
            onRemoveSelectedImage = { viewModel.removeSelectedImage(it) },
            onMediaPickerOpen = { showMediaPicker = true },
            isLoading = state.isLoading,
            isUploadingImages = state.isUploadingImages,
            onSubmit = {
                viewModel.updateTrainerProfile(
                    context = context,
                    hourlyRate = hourlyRate,
                    gymId = selectedGymLocal?.id,
                    description = description,
                    experienceYears = experienceYears,
                    selectedCategories = selectedCategories.map { it.name },
                    images = state.existingImages + state.uploadedImages
                )
            },
            submitTextRes = R.string.save_changes,
            submitEnabled = hourlyRate.isNotBlank() && description.isNotBlank() && experienceYears.isNotBlank() && !state.isUploadingImages,
            onNavigateBack = onNavigateBack
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showGymDialog) {
        GymSelectionDialog(
            gyms = state.gyms,
            onDismiss = { showGymDialog = false },
            onGymSelected = { gym ->
                selectedGymLocal = gym
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
        MediaPickerBottomSheet(
            onDismissRequest = { showMediaPicker = false },
            onSelectImages = { imageLauncher.launch("image/*") },
            onSelectVideos = { videoLauncher.launch("video/*") }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainerEditScreenPreview() {
    TrainerEditScreen()
}
