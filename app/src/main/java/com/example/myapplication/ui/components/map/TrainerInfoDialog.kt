package com.example.myapplication.ui.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerInfoDialog(
    trainer: Trainer?,
    onDismiss: () -> Unit,
    onSeeMore:  (Trainer) -> Unit = {}
) {
    if (trainer != null) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${trainer.firstName} ${trainer.lastName}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("Opis: ${trainer.description ?: "Brak opisu"}")
                Text("Doświadczenie: ${trainer.experience ?: 0} lat")
                Text("Cena za godzinę: ${trainer.pricePerHour ?: 0} zł")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                    TextButton(onClick = { onSeeMore(trainer) }) {
                        Text(stringResource(R.string.see_more))
                    }
                }
            }
        }
    }
}
