package com.example.myapplication.ui.components.user_components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R

@Composable
fun ProfileImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    size: Int = 32,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current) //mozna zmienic nawet na paintera pozniej jak cos
            .data(imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.user_active)
            .error(R.drawable.user_active)
            .build(),
        contentDescription = "User profile image",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White)
    )
}
