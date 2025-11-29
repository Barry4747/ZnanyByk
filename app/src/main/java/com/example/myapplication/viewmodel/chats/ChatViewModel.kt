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
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    lateinit var chatId: String
    lateinit var currentUserId: String

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _receiverAvatarUrl = MutableStateFlow<String?>(null)
    val receiverAvatarUrl: StateFlow<String?> = _receiverAvatarUrl

    fun init(chatId: String, receiverId: String) {
        this.chatId = chatId
        this.currentUserId = authRepository.getCurrentUserId().toString()
        listenForMessages()
        loadReceiverAvatar(receiverId)
    }

    private fun loadReceiverAvatar(receiverId: String) {
        viewModelScope.launch {
            android.util.Log.d("ChatViewModel", "Loading avatar for receiver: $receiverId")
            val avatarResult = userRepository.getAvatarUrl(receiverId)
            _receiverAvatarUrl.value = avatarResult.getOrNull()
            android.util.Log.d("ChatViewModel", "Receiver avatar URL: ${_receiverAvatarUrl.value}")
        }
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

    fun getCurrentUser(): String? {
        return authRepository.getCurrentUserId()
    }

    fun getUserLastName(uid: String): String? {
        return userRepository.getUserSync(uid)?.lastName
    }
    fun getUserFirstName(uid: String): String? {
        return userRepository.getUserSync(uid)?.firstName
    }
    fun getUserFullName(uid: String): String? {
        return getUserFirstName(uid) + " " + getUserLastName(uid)
    }

    fun getUserAvatarUrl(uid: String): String? {
        return userRepository.getUserSync(uid)?.avatarUrl
    }

    suspend fun markSeen() {
        chatRepository.markMessagesAsSeen(chatId, currentUserId)
    }
}