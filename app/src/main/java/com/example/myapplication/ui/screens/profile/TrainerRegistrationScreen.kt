package com.example.myapplication.ui.screens.profile

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
import com.example.myapplication.ui.components.bottomsheets.MediaPickerBottomSheet
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

    TrainerForm(
        modifier = modifier,
        userName = state.userName,
        titleRes = R.string.fillout_trainer_profile,
        hourlyRate = hourlyRate,
        onHourlyRateChange = { input -> hourlyRate = input.filter { it.isDigit() } },
        selectedGymName = selectedGym?.gymName,
        onGymClick = { showGymDialog = true },
        description = description,
        onDescriptionChange = { description = it },
        experienceYears = experienceYears,
        onExperienceChange = { input -> experienceYears = input.filter { it.isDigit() } },
        selectedCategories = selectedCategories,
        onCategoryDialogOpen = { showDialog = true },
        existingImages = emptyList(),
        uploadedImages = state.uploadedImages,
        selectedImages = state.selectedImages,
        isVideoUri = { uri -> viewModel.isVideoUri(context, uri) },
        isVideoUrl = { url -> viewModel.isVideoUrl(url) },
        onRemoveUploadedImage = { viewModel.removeUploadedMedia(it) },
        onRemoveSelectedImage = { viewModel.removeSelectedMedia(it) },
        onMediaPickerOpen = { showMediaPicker = true },
        isLoading = state.isLoading,
        isUploadingImages = state.isUploadingImages,
        onSubmit = {
            viewModel.submitTrainerProfile(
                hourlyRate = hourlyRate,
                gymId = selectedGym?.id,
                description = description,
                experienceYears = experienceYears,
                selectedCategories = selectedCategories.map { it.name },
                images = state.uploadedImages
            )
        },
        submitTextRes = R.string.confirm_trainer_profile_creation,
        submitEnabled = hourlyRate.isNotBlank() && description.isNotBlank() && experienceYears.isNotBlank() && !state.isUploadingImages,
        onNavigateBack = onNavigateBack,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage
    )

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
        MediaPickerBottomSheet(
            onDismissRequest = { showMediaPicker = false },
            onSelectImages = { imageLauncher.launch("image/*") },
            onSelectVideos = { videoLauncher.launch("video/*") }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainerRegistrationScreenPreview() {
    TrainerRegistrationScreen()
}
