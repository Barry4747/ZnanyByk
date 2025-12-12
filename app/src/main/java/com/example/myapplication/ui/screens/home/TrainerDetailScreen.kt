package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.components.GalleryImage
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.dialogs.FullScreenMediaDialog
import com.example.myapplication.ui.components.dialogs.RatingDialog
import com.example.myapplication.ui.components.indicators.RatingIndicator
import com.example.myapplication.viewmodel.TrainerCategory
import com.example.myapplication.viewmodel.TrainersViewModel
import java.util.Locale

data class GalleryItem(
    val uri: String,
    val isVideo: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrainerDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrainersViewModel,
    onBookClick: (String) -> Unit
) {
    val trainersState by viewModel.trainersState.collectAsState()
    val selectedTrainer = trainersState.selectedTrainer
    var showRatingDialog by rememberSaveable { mutableStateOf(false) }
    var selectedImageForZoom by rememberSaveable { mutableStateOf<String?>(null) }

    val primaryTextColor = Color.Black
    val secondaryTextColor = Color.Gray
    val borderColor = Color.DarkGray
    val chipBackgroundColor = Color.DarkGray
    val chipTextColor = Color.White

    if (showRatingDialog) {
        RatingDialog(
            onDismissRequest = { showRatingDialog = false },
            onSubmit = { rating ->
                viewModel.updateUserRating(rating)
                showRatingDialog = false
            },
            initialRating = if (trainersState.currentUserRating == 0) 3 else trainersState.currentUserRating
        )
    }

    if (selectedImageForZoom != null) {
        val isVideo = viewModel.isVideoUrl(selectedImageForZoom!!)
        FullScreenMediaDialog(
            mediaUrl = selectedImageForZoom!!,
            isVideo = isVideo,
            onDismiss = { selectedImageForZoom = null }
        )
    }

    Scaffold(
        bottomBar = {
            if (selectedTrainer != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White)
                ) {
                    MainButton(
                        text = "Umów wizytę",
                        onClick = { onBookClick(selectedTrainer.id ?: "") },
                        modifier = Modifier.fillMaxWidth().testTag("book_visit_btn"),
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            if (selectedTrainer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val imageUrl: Any? = selectedTrainer.images?.firstOrNull()?.let { first ->
                        if (viewModel.isVideoUrl(first)) R.drawable.placeholder else first
                    } ?: R.drawable.placeholder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Zdjęcie trenera: ${selectedTrainer.firstName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter
                        )

                        RatingIndicator(
                            rating = selectedTrainer.avgRating ?: "0.0",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .clickable { showRatingDialog = true }
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("rate_btn")
                        ) {
                            Text(
                                text = "Oceń",
                                color = primaryTextColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    modifier = Modifier.testTag("trainer_fullname"),
                                    text = "${selectedTrainer.firstName} ${selectedTrainer.lastName}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                            }
                            Text(
                                text = "${selectedTrainer.pricePerHour ?: 0} zł/h",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${selectedTrainer.experience ?: 0} lat doświadczenia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Log.d("DIST", trainersState.distanceToTrainer.toString())

                        val distanceText = if (trainersState.distanceToTrainer != null) {
                            "${String.format("%.1f", trainersState.distanceToTrainer)} km od ciebie  •  "
                        } else {
                            ""
                        }

                        Text(
                            text = "$distanceText${selectedTrainer.ratings?.size ?: 0} ocen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrainerCategory.entries
                                .forEach { categoryEnum ->
                                    if (selectedTrainer.categories?.contains(categoryEnum.name) == true) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = chipBackgroundColor,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = categoryEnum.stringResId),
                                                color = chipTextColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "O mnie",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedTrainer.description ?: "Brak opisu.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )

                        var galleryImages: List<String> = selectedTrainer.images ?: emptyList()
                        if (galleryImages.isNotEmpty()) {
                            val firstItem = galleryImages.first()
                            if (!viewModel.isVideoUrl(firstItem)) {
                                galleryImages = galleryImages.drop(1)
                            }
                        }

                        if (galleryImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Galeria",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 1000.dp)
                                    .testTag("gallery_grid"),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp,
                                userScrollEnabled = false
                            ) {
                                items(galleryImages) { imageUrl ->
                                    val isVideo = viewModel.isVideoUrl(imageUrl)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .testTag("gallery_item_$imageUrl")
                                    ) {
                                        GalleryImage(
                                            item = GalleryItem(uri = imageUrl, isVideo = isVideo),
                                            onClick = { selectedImageForZoom = imageUrl }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nie udało się załadować danych trenera.")
                }
            }

            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 8.dp)
            ) {
                MainBackButton(onNavigateBack)
            }
        }
    }
}