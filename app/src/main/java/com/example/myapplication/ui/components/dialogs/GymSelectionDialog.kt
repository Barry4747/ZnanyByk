package com.example.myapplication.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.ui.components.fields.MainFormTextField

@Composable
fun GymSelectionDialog(
    gyms: List<Gym>,
    onDismiss: () -> Unit,
    onGymSelected: (Gym) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredGyms = if (searchQuery.isEmpty()) {
        gyms
    } else {
        gyms.filter { it.gymName.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_gym)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MainFormTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = stringResource(R.string.search),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (filteredGyms.isEmpty()) {
                    Text(
                        text = if (gyms.isEmpty()) {
                            stringResource(R.string.no_gyms_available)
                        } else {
                            stringResource(R.string.no_gyms_found)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(filteredGyms) { gym ->
                            Text(
                                text = gym.gymName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGymSelected(gym) }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Black)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GymSelectionDialogPreview() {
    GymSelectionDialog(
        gyms = listOf(
            Gym(id = "1", gymName = "Siłownia 1"),
            Gym(id = "2", gymName = "Siłownia 2"),
            Gym(id = "3", gymName = "Siłownia 3")
        ),
        onDismiss = {},
        onGymSelected = {}
    )
}
