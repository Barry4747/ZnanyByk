package com.example.myapplication.ui.components.buttons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val BUTTON_HEIGHT = 48.dp
private val BUTTON_CORNER_RADIUS = 12.dp

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black,
    contentColor: Color = if (isSystemInDarkTheme()) Color.Black else Color.White
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT),
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.DarkGray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainButton() {
    MainButton(text = "Sign In", onClick = {})
}