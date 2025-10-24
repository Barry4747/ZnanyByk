package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Chat
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsListState(
    val user: User? = null,
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<ChatsListState>(ChatsListState(isLoading = true))
    val chats: StateFlow<ChatsListState> = _chats
    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCachedUserId()
            if (currentUserId == null) {
                _chats.value = ChatsListState(
                    isLoading = false,
                    errorMessage = "Brak zalogowanego użytkownika"
                )
                return@launch
            }

            _chats.value = ChatsListState(isLoading = true)

            val result = chatRepository.getChatsForUser(currentUserId)

            result.fold(
                onSuccess = { chatList ->
                    _chats.value = ChatsListState(
                        chats = chatList,
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    _chats.value = ChatsListState(
                        isLoading = false,
                        errorMessage = exception.message ?: "Błąd podczas wczytywania chatów"
                    )
                }
            )
        }
    }

    fun getCurrentUser(): String? {
        return userRepository.getCachedUserIdSync()
    }
}
