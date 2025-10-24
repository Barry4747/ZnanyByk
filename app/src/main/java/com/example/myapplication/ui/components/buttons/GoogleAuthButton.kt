package com.example.myapplication.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

private val BUTTON_CORNER_RADIUS = 8.dp
private val BORDER_WIDTH = 1.dp
private val ICON_SIZE = 18.dp
private val ICON_SPACING = 8.dp

@Composable
fun GoogleAuthButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(BORDER_WIDTH, Color.Black)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google__g__logo),
            contentDescription = stringResource(R.string.google_logo),
            modifier = Modifier.size(ICON_SIZE),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(ICON_SPACING))
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleAuthButtonPreview() {
    GoogleAuthButton(
        text = stringResource(R.string.continue_with_google),
        onClick = {}
    )
}
