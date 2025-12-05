package com.example.myapplication.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.viewmodel.chats.ChatServiceViewModel

@Composable
fun MessageButton(
    modifier: Modifier = Modifier,
    userId: String,
    trainerId: String,
    onAppointmentChatClick: (chatId: String, receiverId: String) -> Unit,
    chatServiceViewModel: ChatServiceViewModel = hiltViewModel()
){

    AlternateButton(
        modifier = modifier,
        text = stringResource(R.string.send_message),
        onClick = { chatServiceViewModel.openChat(userId, trainerId, onAppointmentChatClick) },
        enabled = true
    )

}