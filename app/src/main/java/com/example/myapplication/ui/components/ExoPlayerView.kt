package com.example.myapplication.ui.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

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
