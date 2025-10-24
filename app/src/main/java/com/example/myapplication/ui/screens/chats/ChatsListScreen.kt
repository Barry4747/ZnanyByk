package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.Chat
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.ChatsListState
import com.example.myapplication.viewmodel.ChatsListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsListScreen(
    onChatClick: (chatId: String, receiverId: String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel()
) {
    val state by viewModel.chats.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.chats) { chat ->
                        ChatItem(chat = chat, onClick = onChatClick)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: (chatId: String, receiverId: String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel()
) {
    val receiverId = chat.users.firstOrNull { it != viewModel.getCurrentUserId()} ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(chat.id, receiverId) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chat z: $receiverId",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
        Text(
            text = chat.lastTimestamp.toFormattedTime(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}
