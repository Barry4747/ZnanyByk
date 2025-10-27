package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SortScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
)
{
    Column(
        modifier = modifier
            .fillMaxSize() // Wypełnij cały ekran
            .padding(16.dp), // Dodaj marginesy
        horizontalAlignment = Alignment.CenterHorizontally, // Wycentruj elementy w poziomie
        verticalArrangement = Arrangement.Center // Wycentruj elementy w pionie
    ) {
        // Tytuł ekranu
        Text(
            text = "Ekran Sortowania",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Przycisk do powrotu
        Button(onClick = onNavigateBack) { // Wywołaj funkcję onNavigateBack po kliknięciu
            Text(text = "Wróć do listy")
        }
    }
}