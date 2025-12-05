import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val BUTTON_HEIGHT = 56.dp
private val BUTTON_CORNER_RADIUS = 12.dp
private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f
private val BORDER_WIDTH = 1.dp
private val HORIZONTAL_PADDING = 16.dp
private val VERTICAL_PADDING = 16.dp


@Composable
fun FormButtonWithDetail(
    text: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT),
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
        ),
        border = BorderStroke(
            width = BORDER_WIDTH,
            color = if (enabled) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA)
            }
        ),
        contentPadding = PaddingValues(horizontal = HORIZONTAL_PADDING, vertical = VERTICAL_PADDING)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormButtonWithDetailPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormButtonWithDetail(
                    text = "Język",
                    detail = "Polski",
                    onClick = {}
                )
                FormButtonWithDetail(
                    text = "Lokalizacja",
                    detail = "Tokarowice Dolne, Piękna 47",
                    onClick = {}
                )
                FormButtonWithDetail(
                    text = "Waluta",
                    detail = "PLN, zł",
                    onClick = {}
                )
                FormButtonWithDetail(
                    text = "Disabled",
                    detail = "Cannot change",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}