package com.example.myapplication.ui.screens.chats

import MainProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.chats.Chat
import com.example.myapplication.ui.components.user_components.ProfileImage
import com.example.myapplication.viewmodel.chats.ChatsListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun ChatsListScreen(
    onChatClick: (chatId: String, receiverId: String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel()
) {
    val state by viewModel.chats.collectAsState()
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.chats), style = MaterialTheme.typography.titleLarge)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            )
        }


        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    MainProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: (chatId: String, receiverId: String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel()
) {
    val currentUserId = viewModel.getCurrentUserId()
    val receiverId = chat.users.firstOrNull { it != currentUserId } ?: ""
    val receiverFirstName = viewModel.getUserFirstName(receiverId) ?: "User"

    val lastMsgUnseen = !chat.lastMessageSeen && chat.lastMessageSender != currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(chat.id, receiverId) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lastMsgUnseen) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        ProfileImage(imageUrl = null, modifier = Modifier.size(32.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = receiverFirstName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = chat.lastTimestamp.toRelativeTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (lastMsgUnseen) FontWeight.Bold else FontWeight.Normal
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = (now - this).absoluteValue

    val oneDay = 24 * 60 * 60 * 1000L
    return if (diff > oneDay) {
        val days = diff / oneDay
        "$days dni temu"
    } else {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(this))
    }
}