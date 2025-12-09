package com.example.myapplication.ui.components.user_components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.R

@Composable
fun ProfilePicture(
    model : Any,
    size: Int = 32,
    borderSize: Int = 2,
    placeholderResource: Int = R.drawable.user_active
) {
    AsyncImage(
        model = model,
        contentDescription = stringResource(R.string.profile_picture),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(borderSize.dp, Color.Black, CircleShape),
        error = painterResource(placeholderResource),
        placeholder = painterResource(placeholderResource)
    )
}

