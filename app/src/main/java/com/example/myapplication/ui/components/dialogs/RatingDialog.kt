package com.example.myapplication.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun RatingDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (rating: Int) -> Unit,
    initialRating: Int = 0 // Zmieniono domyślnie na 0 (brak oceny), ale 3 też jest ok
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Oceń trenera",
                    style = MaterialTheme.typography.titleLarge
                )

                var currentRating by remember { mutableIntStateOf(if (initialRating == 0) 0 else initialRating) }

                // Tutaj wstawiamy nasze gwiazdki zamiast slidera
                StarRatingBar(
                    rating = currentRating,
                    onRatingChanged = { newRating ->
                        currentRating = newRating
                    }
                )

                // Tekst pomocniczy (opcjonalny)
                Text(
                    text = if (currentRating > 0) "Twoja ocena: $currentRating/5" else "Wybierz ocenę",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Anuluj")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (currentRating > 0) {
                                onSubmit(currentRating)
                            }
                        },
                        enabled = currentRating > 0 // Przycisk aktywny tylko gdy wybrano ocenę
                    ) {
                        Text("Zatwierdź")
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Gwiazdka $i",
                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray, // Złoty kolor dla pełnych gwiazdek
                modifier = Modifier
                    .size(40.dp) // Rozmiar gwiazdki
                    .clickable { onRatingChanged(i) } // Kliknięcie ustawia ocenę
                    .padding(4.dp)
            )
        }
    }
}
