package com.example.myapplication.ui.components.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.TrainerCategory

@Composable
fun SelectionCategoryChip(
    modifier: Modifier = Modifier,
    category: TrainerCategory,
    onSelectionChange: ((Boolean) -> Unit)? = null
) {
    var selected by remember { mutableStateOf(false) }

    FilterChip(
        selected = selected,
        onClick = {
            selected = true
            onSelectionChange?.invoke(true)
        },
        label = { Text(stringResource(category.labelRes)) },
        modifier = modifier,
        enabled = !selected
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectionCategoryChipPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectionCategoryChip(category = TrainerCategory.CROSSFIT)
        SelectionCategoryChip(category = TrainerCategory.YOGA)
    }
}
