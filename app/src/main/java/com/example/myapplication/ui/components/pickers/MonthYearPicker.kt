
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MonthYearPicker(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    val months = listOf(
        "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
        "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
    )
    val years = (2000..2030).toList()

    var selectedMonth by remember { mutableStateOf(initialMonth - 1) }
    var selectedYear by remember { mutableStateOf(years.indexOf(initialYear).takeIf { it >= 0 } ?: 0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Wybierz miesiąc i rok",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RevoPicker(
                        items = months,
                        initialIndex = selectedMonth,
                        onSnapped = { selectedMonth = it },
                        modifier = Modifier.weight(1f)
                    )
                    RevoPicker(
                        items = years.map { it.toString() },
                        initialIndex = selectedYear,
                        onSnapped = { selectedYear = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj", color = androidx.compose.ui.graphics.Color.Black)
                    }
                    TextButton(onClick = {
                        onConfirm(selectedMonth + 1, years[selectedYear])
                        onDismiss()
                    }) {
                        Text("OK", color = androidx.compose.ui.graphics.Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevoPicker(
    items: List<String>,
    initialIndex: Int,
    onSnapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 40.dp
    val visibleCount = 5
    val halfVisible = visibleCount / 2

    // Dodaj puste elementy na początku i końcu, aby umożliwić przewijanie
    val paddedItems = List(halfVisible) { "" } + items + List(halfVisible) { "" }
    val adjustedInitialIndex = initialIndex + halfVisible

    // Przewiń do pozycji wyśrodkowanej: targetIndex - halfVisible
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (adjustedInitialIndex - halfVisible).coerceAtLeast(0)
    )
    val coroutine = rememberCoroutineScope()
    val density = LocalDensity.current

    // Zgłoś początkową wartość
    LaunchedEffect(Unit) {
        onSnapped(initialIndex)
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleCount)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        )
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(paddedItems.size) { index ->
                val centerIndex = listState.firstVisibleItemIndex + halfVisible
                val distance = abs(centerIndex - index)
                val scale = 1f - (distance * 0.15f).coerceAtLeast(0f)

                // Ukryj puste elementy padding
                val item = paddedItems[index]
                if (item.isNotEmpty()) {
                    Text(
                        text = item,
                        fontSize = (16 * scale).sp,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (distance == 0)
                            androidx.compose.ui.graphics.Color.Black
                        else
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                } else {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(120)
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val offsetIndex = if (scrollOffset > itemHeightPx / 2) 1 else 0
            val targetIndex = (listState.firstVisibleItemIndex + halfVisible + offsetIndex)
                .coerceIn(halfVisible, paddedItems.lastIndex - halfVisible)

            coroutine.launch {
                listState.animateScrollToItem(targetIndex - halfVisible)
                // Zwróć indeks bez paddingu
                onSnapped((targetIndex - halfVisible).coerceIn(0, items.lastIndex))
            }
        }
    }
}