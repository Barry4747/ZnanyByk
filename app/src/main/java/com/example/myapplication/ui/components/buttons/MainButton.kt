package com.example.myapplication.ui.components.buttons

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

private val BUTTON_CORNER_RADIUS = 8.dp

@Composable
fun MainButton(
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
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}


@Preview(showBackground = true)
@Composable
private fun MainButtonPreview() {
    MainButton(
        text = stringResource(R.string.login),
        onClick = {}
    )
}
