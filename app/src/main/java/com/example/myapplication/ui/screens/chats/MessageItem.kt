package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.chats.Message
import com.example.myapplication.ui.components.user_components.ProfileImage

@Composable
fun MessageItem(
    message: Message,
    isCurrentUser: Boolean,
    showProfile: Boolean,
    timestamp: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (timestamp.isNotEmpty()) {
            Text(
                text = timestamp,
                fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isCurrentUser) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.LightGray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.text.trim(),
                        color = if (isCurrentUser) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black,
                        fontSize = 18.sp
                    )
                }
            }

            if (showProfile) {
                Spacer(modifier = Modifier.height(4.dp))
                ProfileImage(
                    imageUrl = if (!isCurrentUser) null else null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}