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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.components.GalleryImage
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.dialogs.FullScreenMediaDialog
import com.example.myapplication.ui.components.dialogs.RatingDialog
import com.example.myapplication.ui.components.indicators.RatingIndicator
import com.example.myapplication.viewmodel.TrainersViewModel

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
                        .background(Color.White) // Ensure background behind button is clean
                ) {
                    Button(
                        onClick = { onBookClick(selectedTrainer.id ?: "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black, // Black Button
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Umów wizytę",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (selectedTrainer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- HEADER IMAGE SECTION ---
                    val imageUrl: Any? = if (selectedTrainer.images?.isNotEmpty() == true) {
                        selectedTrainer.images!!.let { images ->
                            if (viewModel.isVideoUrl(images[0])) R.drawable.placeholder else images[0]
                        }
                    } else {
                        R.drawable.placeholder
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Slightly taller for detail view
                            .background(Color.White)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Zdjęcie trenera: ${selectedTrainer.firstName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter // Match Card: TopCenter alignment
                        )

                        // "Rate" Button inside Image (Styled like a Chip)
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
                        ) {
                            Text(
                                text = "Oceń",
                                color = primaryTextColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // --- INFO SECTION ---
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
                                color = primaryTextColor // Match Card: Black Price
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${selectedTrainer.experience ?: 0} lat doświadczenia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor // Match Card: Gray subtext
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

                        // --- CATEGORIES (Match Card: Dark Gray BG, White Text) ---
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedTrainer.categories?.forEach { speciality ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = chipBackgroundColor, // Dark Gray
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = speciality,
                                        color = chipTextColor, // White
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
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

                        // --- GALLERY SECTION ---
                        var galleryImages: List<String> = selectedTrainer.images ?: emptyList()
                        if (galleryImages.isNotEmpty()) {
                            val firstItem = galleryImages.first()
                            // Logic: Skip first image if it's the one shown in header (unless it's a video)
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
                                    .heightIn(max = 1000.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp,
                                userScrollEnabled = false
                            ) {
                                items(galleryImages) { imageUrl ->
                                    val isVideo = viewModel.isVideoUrl(imageUrl)
                                    // Wrap GalleryImage to enforce shape/border consistency
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                    ) {
                                        GalleryImage(
                                            item = GalleryItem(uri = imageUrl, isVideo = isVideo),
                                            onClick = { selectedImageForZoom = imageUrl }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom button
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

            MainBackButton(onNavigateBack)

            if (selectedTrainer != null) {
                RatingIndicator(
                    rating = selectedTrainer.avgRating ?: "0.0",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
