package com.example.myapplication.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SortOption

@Composable
fun SortDialog(
    currentSortOption: SortOption = SortOption.PRICE_ASC,
    onDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 360.dp, max = 600.dp),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = Color.Black)
            }
        },
        title = { Text("Sortuj według") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOption.entries.forEach { option ->
                        FilterChip(
                            selected = option == currentSortOption,
                            onClick = {
                                onSortSelected(option)
                                onDismiss()
                            },
                            label = { Text(option.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    )
}