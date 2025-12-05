package com.example.myapplication.viewmodel.chats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.model.chats.Chat
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ChatUIModel(
    val chat: Chat,
    val receiverId: String,
    val receiverName: String,
    val receiverAvatarUrl: String?,
    val isUnread: Boolean
)

data class ChatsListState(
    val chatItems: List<ChatUIModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _chats = MutableStateFlow<ChatsListState>(ChatsListState(isLoading = true))
    val chats: StateFlow<ChatsListState> = _chats

    private val _state = MutableStateFlow(ChatsListState(isLoading = true))
    val state: StateFlow<ChatsListState> = _state

    init {
        listenForChats()
    }

    private fun listenForChats() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _state.value = ChatsListState(errorMessage = appContext.getString(R.string.no_user_logged_in))
            return
        }

        viewModelScope.launch {
            chatRepository.getChatsForUserFlow(currentUserId).collect { chats ->

                val uiModels = withContext(Dispatchers.IO) {
                    chats.map { chat ->
                        val receiverId = chat.users.firstOrNull { it != currentUserId } ?: ""

                        val user = userRepository.getUserSync(receiverId)

                        val receiverName = user?.firstName ?: appContext.getString(R.string.user)
                        val receiverAvatar = user?.avatarUrl

                        val isUnread = !chat.lastMessageSeen && chat.lastMessageSender != currentUserId

                        ChatUIModel(
                            chat = chat,
                            receiverId = receiverId,
                            receiverName = receiverName,
                            receiverAvatarUrl = receiverAvatar,
                            isUnread = isUnread
                        )
                    }
                }

                _state.value = ChatsListState(
                    chatItems = uiModels,
                    isLoading = false
                )
            }
        }
    }

    fun getCurrentUserId(): String? {
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

}
