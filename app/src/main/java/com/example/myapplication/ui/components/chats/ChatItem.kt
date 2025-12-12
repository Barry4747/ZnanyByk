package com.example.myapplication.ui.components.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.components.user_components.ProfilePicture
import com.example.myapplication.ui.screens.chats.toRelativeTime
import com.example.myapplication.viewmodel.chats.ChatUIModel

@Composable
fun ChatItem(
    item: ChatUIModel,
    onClick: (chatId: String, receiverId: String) -> Unit
) {
    val avatarResource = item.receiverAvatarUrl ?: R.drawable.user_active

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.chat.id, item.receiverId) }
            .padding(12.dp)
            .testTag("chatItem_${item.chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.isUnread) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .testTag("unreadIndicator")
            )
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))
        ProfilePicture(model = avatarResource, size = 32, borderSize = 1)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.receiverName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("chatItemName")
                )

                Text(
                    text = item.chat.lastTimestamp.toRelativeTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.testTag("chatItemTime")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (item.isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.isUnread) Color.Black else Color.Gray
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.testTag("chatItemLastMessage")
            )
        }
    }
}