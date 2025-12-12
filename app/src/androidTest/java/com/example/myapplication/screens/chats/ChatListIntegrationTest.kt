package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.chats.ChatScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.chats.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@HiltAndroidTest
class ChatScreenIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        auth.signInWithEmailAndPassword("david.laid@gmail.com", "qqqqqq")
        Thread.sleep(3000)
    }

    @Test
    fun sendMessage_appearsInList_and_firebase() {
        val uniqueChatId = "Du05NQlctAl3q98F9RQD"
        val receiverId = "test_receiver_id"
        val messageText = "Hello Integration ${UUID.randomUUID().toString().take(5)}"

        lateinit var viewModel: ChatViewModel

        composeRule.setContent {
            MyApplicationTheme {
                viewModel = hiltViewModel<ChatViewModel>()
                ChatScreen(
                    chatId = uniqueChatId,
                    receiverId = receiverId,
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithTag("messageInput")
            .performClick()
            .performTextInput(messageText)

        composeRule.onNodeWithTag("sendButton")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodesWithText(messageText).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(messageText).assertIsDisplayed()

        composeRule.runOnIdle {
            val messages = viewModel.messages.value
            val found = messages.any { it.text == messageText }

            if (!found) {
                throw AssertionError("Brak wiadomości w state ViewModelu!")
            }
        }
    }
}