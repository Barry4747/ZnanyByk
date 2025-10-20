package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun MainTextButton(
    text: String,
    onClick: () -> Unit = {},
    enabled : Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text,
            textDecoration = TextDecoration.Underline
        )
    }
}