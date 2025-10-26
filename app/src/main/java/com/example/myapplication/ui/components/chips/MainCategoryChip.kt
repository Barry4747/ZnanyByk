package com.example.myapplication.ui.components.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.users.TrainerCategory

@Composable
fun MainCategoryChip(
    modifier: Modifier = Modifier,
    category: TrainerCategory? = null,
    label: String = "",
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    AssistChip(
        onClick = onClick,
        label = { Text(if (category != null) stringResource(category.labelRes) else label) },
        modifier = modifier,
        enabled = enabled
    )
}

@Preview(showBackground = true)
@Composable
private fun MainCategoryChipPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MainCategoryChip(category = TrainerCategory.CROSSFIT)
        MainCategoryChip(label = "+")
        MainCategoryChip(category = TrainerCategory.YOGA, enabled = false)
    }
}
