package com.example.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun MainBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag("backButton")
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(id=R.drawable.left_arrow),
            contentDescription = stringResource(R.string.back),
            tint = tint
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainBackButtonPreview() {
    MainBackButton(onClick = {})
}
