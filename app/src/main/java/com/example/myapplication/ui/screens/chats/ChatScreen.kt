package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import com.example.myapplication.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.ui.components.buttons.MainBackButton
import com.example.myapplication.ui.components.chats.MessageItem
import com.example.myapplication.ui.components.inputs.MessageInputBar
import com.example.myapplication.ui.components.user_components.ProfilePicture
import com.example.myapplication.utils.shouldShowProfile
import com.example.myapplication.utils.getSmartTimestamp
import com.example.myapplication.viewmodel.chats.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    chatId: String,
    receiverId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()



    DisposableEffect(Unit) {
        viewModel.init(chatId, receiverId)
        coroutineScope.launch {
            viewModel.markSeen()
        }

        onDispose {
            coroutineScope.launch {
                viewModel.markSeen()
            }
        }
    }

    val currentUserId = viewModel.getCurrentUser()
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    val receiverAvatarUrl by viewModel.receiverAvatarUrl.collectAsState()
    val avatarResource = receiverAvatarUrl ?: R.drawable.user_active
    val receiverName by viewModel.receiverName.collectAsState()

    LaunchedEffect(isFocused, messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .imePadding()
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)) {
            Row(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MainBackButton(onClick = onNavigateBack)
                    ProfilePicture(
                        model = avatarResource,
                        size = 40,
                        borderSize = 2
                    )
                    Text(
                        text = receiverName,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                }
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
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                val isCurrentUser = message.senderId == currentUserId

                val newerMessage = messages.getOrNull(index - 1)

                val olderMessage = messages.getOrNull(index + 1)

                val showProfile = shouldShowProfile(
                    index,
                    messages,
                    currentUserId
                )
                val timestampText = getSmartTimestamp(message.timestamp, olderMessage?.timestamp)

                MessageItem(
                    message = message,
                    avatarResource = avatarResource,
                    isCurrentUser = isCurrentUser,
                    showProfile = showProfile,
                    timestamp = timestampText
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            MessageInputBar(
                onSendMessage = { messageText ->
                    viewModel.sendMessage(receiverId, messageText)
                }
            )
        }
    }
}
