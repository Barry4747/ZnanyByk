package com.example.myapplication.viewmodel.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.chats.Message
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatServiceViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private suspend fun getOrCreateChat(clientId: String, trainerId: String): String {
        return chatRepository.createChatIfNotExists(clientId, trainerId).toString()
    }

    fun openChat(clientId: String, trainerId: String, onChatReady: (chatId: String, receiverId: String) -> Unit) {
        viewModelScope.launch {
            val chatId = getOrCreateChat(clientId, trainerId)
            onChatReady(chatId.toString(), trainerId)
        }
    }

}