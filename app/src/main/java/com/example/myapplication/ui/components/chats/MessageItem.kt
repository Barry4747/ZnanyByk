package com.example.myapplication.ui.components.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.chats.Message
import com.example.myapplication.ui.components.user_components.ProfilePicture

@Composable
fun MessageItem(
    message: Message,
    avatarResource: Any,
    isCurrentUser: Boolean,
    showProfile: Boolean,
    timestamp: String
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.75f

    val bubbleColor = if (isCurrentUser) Color.Black else Color(0xFFE4E6EB)
    val textColor = if (isCurrentUser) Color.White else Color.Black

    val bubbleShape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        if (timestamp.isNotEmpty()) {
            Text(
                text = timestamp,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {

            if (!isCurrentUser) {
                if (showProfile) {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        ProfilePicture(
                            model = avatarResource,
                            size = 28,
                            borderSize = 0
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }

            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                modifier = Modifier.widthIn(max = maxBubbleWidth)
            ) {
                Text(
                    text = message.text.trim(),
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}