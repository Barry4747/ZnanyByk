package com.example.myapplication.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Message
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    lateinit var chatId: String
    lateinit var currentUserId: String

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun init(chatId: String) {
        this.chatId = chatId
        this.currentUserId = userRepository.getCachedUserIdSync().toString()
        listenForMessages()
    }

    private fun listenForMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesForChatFlow(chatId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(receiverId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, currentUserId, receiverId, text)
        }
    }
}
