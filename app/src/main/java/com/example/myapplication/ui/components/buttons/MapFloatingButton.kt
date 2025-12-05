package com.example.myapplication.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun MapFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.DarkGray,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.map_search_icon),
            contentDescription = stringResource(R.string.search_on_map),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
fun MapFloatingButtonExample(){
    MapFloatingButton(onClick = {})
}