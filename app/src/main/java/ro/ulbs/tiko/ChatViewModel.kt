package ro.ulbs.tiko

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception

// Represents the UI state for the chat screen.
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(private val repository: ChatHistoryRepository) : ViewModel() {

    private val openAIService: OpenAIService = OpenAIClient.create()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val history = repository.chatHistory.first()
            if (history.isEmpty()) {
                val welcomeMessage = ChatMessage(
                    text = "Hello! I'm Tiko, your ULBS assistant. How can I help you today?",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveChatHistory(listOf(welcomeMessage))
                _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
            } else {
                _uiState.update { it.copy(messages = history) }
            }
        }
    }

    /**
     * Builds the request for the OpenAI Chat API, including the system prompt and message history.
     */
    private fun buildChatRequest(history: List<ChatMessage>): OpenAIRequest {
        val systemMessage = Message(role = "system", content = TIKO_SYSTEM_PROMPT)

        val chatMessages = history.map { message ->
            Message(
                role = if (message.isUser) "user" else "assistant",
                content = message.text
            )
        }

        return OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(systemMessage) + chatMessages
        )
    }

    fun sendMessage(userText: String) {
        val userMessage = ChatMessage(text = userText, isUser = true, timestamp = System.currentTimeMillis())
        val updatedMessages = _uiState.value.messages + userMessage

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                isLoading = true,
                error = null // Clear previous errors
            )
        }

        // Check for FAQ answer first
        val faqAnswer = findFaqAnswer(userText)
        if (faqAnswer != null) {
            val assistantMessage = ChatMessage(text = faqAnswer, isUser = false, timestamp = System.currentTimeMillis())
            val finalMessages = updatedMessages + assistantMessage
            viewModelScope.launch {
                repository.saveChatHistory(finalMessages)
                _uiState.update { it.copy(messages = finalMessages, isLoading = false) }
            }
            return
        }

        // If no FAQ answer, call OpenAI
        viewModelScope.launch {
            try {
                repository.saveChatHistory(updatedMessages)
                val request = buildChatRequest(updatedMessages)
                val response = openAIService.getChatCompletion(request)

                response.choices.firstOrNull()?.message?.content?.let { assistantText ->
                    val assistantMessage = ChatMessage(text = assistantText, isUser = false, timestamp = System.currentTimeMillis())
                    val finalMessages = updatedMessages + assistantMessage
                    repository.saveChatHistory(finalMessages)
                    _uiState.update {
                        it.copy(
                            messages = finalMessages,
                            isLoading = false
                        )
                    }
                } ?: _uiState.update { it.copy(error = "Assistant did not provide a response.", isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "An error occurred: ${e.message}", isLoading = false) }
            }
        }
    }
}
