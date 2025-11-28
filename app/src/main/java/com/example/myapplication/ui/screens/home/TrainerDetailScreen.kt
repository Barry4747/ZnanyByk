package com.example.myapplication.ui.screens.home

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.components.dialogs.RatingDialog
import com.example.myapplication.viewmodel.TrainersViewModel
import androidx.core.net.toUri


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

    if (showRatingDialog) {
        RatingDialog(
            onDismissRequest = {
                showRatingDialog = false
            },
            onSubmit = { rating ->
                Log.d("RatingDialog", "Trener oceniony na: $rating")
                Log.d("RatingDialog", "Trener id: ${selectedTrainer}")
                viewModel.updateUserRating(rating)
                showRatingDialog = false
            },
            initialRating = if (trainersState.currentUserRating == 0) 3 else trainersState.currentUserRating

        )
    }

    if (selectedImageForZoom != null) {
        val isVideo = viewModel.isVideoUrl(selectedImageForZoom!!)
        Log.d("TrainerDetailScreen", "Opening full screen media. URL: $selectedImageForZoom, isVideo: $isVideo")
        FullScreenMediaDialog(
            mediaUrl = selectedImageForZoom!!,
            isVideo = isVideo,
            onDismiss = { selectedImageForZoom = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTrainer != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val imageUrl: Any? = if (selectedTrainer.images?.isNotEmpty() == true) {
                    selectedTrainer.images!!.let { images ->
                        if (viewModel.isVideoUrl(images[0])) {
                            R.drawable.placeholder
                        } else {
                            images[0]
                        }
                    }
                } else {
                    R.drawable.placeholder
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Zdjęcie trenera: ${selectedTrainer.firstName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    TextButton(
                        onClick = {showRatingDialog = true},
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "Oceń",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedTrainer.firstName} ${selectedTrainer.lastName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedTrainer.pricePerHour ?: 0} zł/h",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "${selectedTrainer.experience ?: 0} lat doświadczenia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "${selectedTrainer.gymId ?: "-"} km od ciebie     ${selectedTrainer.ratings?.size ?: 0} ocen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTrainer.categories?.forEach { speciality ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = speciality,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "O mnie",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedTrainer.description ?: "Brak opisu.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    var galleryImages: List<String> = selectedTrainer.images ?: emptyList()


                    if (galleryImages.isNotEmpty()) {
                        val firstItem = galleryImages.first()

                        if (!viewModel.isVideoUrl(firstItem)) {
                            galleryImages = galleryImages.drop(1)
                        }
                    }

                    Log.d("TrainerDetailScreen", "Gallery images: $galleryImages")

                    if (galleryImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Galeria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val rowCount = (galleryImages.size + 1) / 2

                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max=1000.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            userScrollEnabled = false
                        ) {
                            items(galleryImages) { imageUrl ->
                                val isVideo = viewModel.isVideoUrl(imageUrl)
                                Log.d("TrainerDetailScreen", "Processing gallery item: $imageUrl, isVideo: $isVideo")
                                GalleryImage(
                                    item = GalleryItem(uri = imageUrl, isVideo = isVideo),
                                    onClick = {selectedImageForZoom =  imageUrl}
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nie udało się załadować danych trenera. Wróć i spróbuj ponownie.")
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 24.dp)
                .size(32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Wróć",
                tint = Color.White
            )
        }

        if (selectedTrainer != null) {
            RatingIndicator(
                rating = selectedTrainer.avgRating ?: "0.0",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }


        Button(
            onClick = { onBookClick(selectedTrainer?.id ?: "") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Umów wizytę")
        }
    }
}

@Composable
fun RatingIndicator(
    rating: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating star",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = rating,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GalleryImage(
    item: GalleryItem,
    onClick: (GalleryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick(item) }
    ) {

        AsyncImage(
            model = item.uri,
            contentDescription = if (item.isVideo) "Wideo z galerii" else "Zdjęcie z galerii",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )


        if (item.isVideo) {
            Log.d("GalleryImage", "Showing play button for video")
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Odtwórz wideo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun FullScreenMediaDialog(
    mediaUrl: String,
    isVideo: Boolean,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            if (isVideo) {
                ExoPlayerView(mediaUrl)
            } else {
                Log.d("FullScreenMediaDialog", "Showing image: $mediaUrl")
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = "Powiększone zdjęcie",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun ExoPlayerView(mediaUrl: String) {
    val context = LocalContext.current

    // 1. Inicjalizacja ExoPlayera
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl.toUri()))
            prepare()
        }
    }

    // 2. Zarządzanie cyklem życia (KLUCZOWE)
    DisposableEffect(key1 = Unit) {
        // Rozpocznij odtwarzanie, gdy kompozycja jest aktywna
        exoPlayer.playWhenReady = true

        onDispose {
            // Zatrzymaj i zwolnij zasoby, gdy kompozycja znika
            exoPlayer.release()
        }
    }

    // 3. Wyświetlenie odtwarzacza za pomocą AndroidView
    AndroidView(
        factory = {
            // PlayerView to UI opakowujące ExoPlayer
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
