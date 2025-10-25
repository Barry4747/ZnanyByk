package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.myapplication.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.user_components.ProfileImage
import com.example.myapplication.utils.shouldShowTimestamp
import com.example.myapplication.utils.shouldShowProfile
import com.example.myapplication.utils.formatTimestamp
import com.example.myapplication.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    chatId: String,
    receiverId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.init(chatId)
        viewModel.markSeen()
    }

    val receiverProfileUrl = null
    val currentUserId = viewModel.getCurrentUser()
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            Row(
                modifier = Modifier.padding(8.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainBackButton(onClick = onNavigateBack)
                ProfileImage(imageUrl = receiverProfileUrl)
                Text(
                    text = viewModel.getUserFullName(receiverId).toString(),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                val isCurrentUser = message.senderId == currentUserId
                val previousMessage = messages.getOrNull(index - 1)

                val showProfile = shouldShowProfile(index, messages, currentUserId)
                val showTimestamp = shouldShowTimestamp(message.timestamp, previousMessage?.timestamp)
                val timestampText = formatTimestamp(message.timestamp, previousMessage?.timestamp)

                MessageItem(
                    message = message,
                    isCurrentUser = isCurrentUser,
                    showProfile = showProfile,
                    timestamp = if (showTimestamp) timestampText else ""
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    if (!isFocused) Text(
                        "Napisz wiadomość...",
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .background(Color.White.copy(alpha = 0.0f))
                    .onFocusChanged { focusState -> isFocused = focusState.isFocused }
                    .heightIn(min = 56.dp, max = 150.dp),
                trailingIcon = {
                    if (isFocused) {
                        IconButton(onClick = {
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(receiverId, text)
                                text = ""
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.send_arrow),
                                contentDescription = "Send"
                            )
                        }
                    }
                },
                maxLines = 5,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black
                )
            )
        }
    }
}
