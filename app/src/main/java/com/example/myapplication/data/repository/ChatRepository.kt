package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Chat
import com.example.myapplication.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")

    suspend fun getChatsForUser(userId: String): Result<List<Chat>> {
        return try {
            val snapshot = chatsCollection
                .whereArrayContains("users", userId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val chats = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }

            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(chatId: String): Result<List<Message>> {
        return try {
            val snapshot = chatsCollection
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val messages = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            }

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getMessagesForChatFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val chatRef = chatsCollection.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener: ListenerRegistration = chatRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(messages).isSuccess
        }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        receiverId: String,
        text: String
    ): Result<Unit> {
        return try {
            val message = Message(
                chatId = chatId,
                senderId = currentUserId,
                receiverId = receiverId,
                text = text
            )

            val chatRef = chatsCollection.document(chatId)

            db.runBatch { batch ->
                val msgRef = chatRef.collection("messages").document()
                batch.set(msgRef, message)

                batch.set(
                    chatRef,
                    mapOf(
                        "users" to listOf(currentUserId, receiverId),
                        "lastMessage" to text,
                        "lastTimestamp" to message.timestamp
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createChatIfNotExists(userA: String, userB: String): Result<String> {
        return try {
            val snapshot = chatsCollection
                .whereArrayContains("users", userA)
                .get()
                .await()

            val existingChat = snapshot.documents.firstOrNull { doc ->
                val users = doc.get("users") as? List<*>
                users?.contains(userB) == true
            }

            if (existingChat != null) {
                Result.success(existingChat.id)
            } else {
                val chatData = mapOf(
                    "users" to listOf(userA, userB),
                    "lastMessage" to "",
                    "lastTimestamp" to System.currentTimeMillis()
                )
                val newChatRef = chatsCollection.add(chatData).await()
                Result.success(newChatRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
