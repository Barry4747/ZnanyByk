package com.example.myapplication.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun MessageInputBar(
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    // Kolor tła dla pola tekstowego (jasnoszary)
    val inputFieldBackgroundColor = Color(0xFFF3F4F6)
    // Kolor ikony wysyłania (np. primary color z Twojego motywu lub własny niebieski)
    val sendIconColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White) // Tło całego paska na dole
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pole tekstowe
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = stringResource(R.string.write_message),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier
                .weight(1f)
                // Kluczowe dla wyglądu "pigułki":
                .clip(RoundedCornerShape(24.dp))
                .background(inputFieldBackgroundColor)
                .onFocusChanged { focusState -> isFocused = focusState.isFocused }
                .heightIn(min = 50.dp, max = 150.dp), // Trochę mniejsza wysokość minimalna wygląda zgrabniej
            maxLines = 5,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black
            ),
            // Magia usuwania domyślnych stylów Material Design (podkreśleń, tła):
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent, // Usuwa podkreślenie paska
                unfocusedIndicatorColor = Color.Transparent, // Usuwa podkreślenie paska
            ),
            // Ikona wysyłania wewnątrz pola (opcjonalnie, jeśli chcesz jak w Messengerze)
            // Jeśli wolisz ikonę obok pola, usuń trailingIcon stąd i odkomentuj IconButton poniżej.
            trailingIcon = {
                val showSendButton = isFocused || text.isNotBlank()
                if (showSendButton) {
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSendMessage(text)
                                text = ""
                            }
                        },
                        // Dodajemy trochę paddingu, żeby ikona nie była przyklejona do krawędzi
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send_arrow),
                            contentDescription = stringResource(R.string.send),
                            // Kolorujemy ikonę tylko gdy jest tekst do wysłania
                            tint = if (text.isNotBlank()) sendIconColor else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        )

        // OPCJA ALTERNATYWNA: Przycisk wysyłania OBOK pola (jak w WhatsApp)
        // Jeśli wolisz ten styl, zakomentuj trailingIcon w TextField powyżej i odkomentuj to:
        /*
        val showSendButton = text.isNotBlank()
        AnimatedVisibility(visible = showSendButton) {
             IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                modifier = Modifier
                    .padding(bottom = 4.dp) // Wyrównanie do dołu przy wielowierszowym tekście
                    .clip(CircleShape)
                    .background(sendIconColor) // Kolorowe tło dla przycisku
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send_arrow),
                    contentDescription = stringResource(R.string.send),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        */
    }
}