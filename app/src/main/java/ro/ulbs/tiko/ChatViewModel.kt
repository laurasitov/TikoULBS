package ro.ulbs.tiko

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the chat screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val openAIService: OpenAIService = OpenAIClient.create()
    private val repository = ChatHistoryRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Add a welcome message on first launch
        if (repository.messages.isEmpty()) {
            repository.addAssistantMessage("Hello! I'm Tiko, your ULBS assistant. How can I help you today?")
        }
        _uiState.update { it.copy(messages = repository.messages) }
    }

    fun sendMessage(userText: String) {
        repository.addUserMessage(userText)
        _uiState.update {
            it.copy(
                messages = repository.messages,
                isLoading = true,
                error = null // Clear previous errors
            )
        }

        // Launch a coroutine to handle the request
        viewModelScope.launch {
            try {
                val request = buildChatRequest(repository.messages)
                val response = openAIService.getChatCompletion(request)

                response.choices.firstOrNull()?.messagePayload?.content?.let { assistantText ->
                    repository.addAssistantMessage(assistantText)
                    _uiState.update {
                        it.copy(
                            messages = repository.messages,
                            isLoading = false
                        )
                    }
                } ?: _uiState.update { it.copy(error = "Assistant did not provide a response.", isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "An error occurred: ${e.message}", isLoading = false) }
            }
        }
    }

    private fun buildChatRequest(history: List<ChatMessage>): ChatRequest {
        val systemMessage = MessagePayload(role = "system", content = TIKO_SYSTEM_PROMPT)

        val chatMessages = history.map { message ->
            MessagePayload(
                role = message.role,
                content = message.content
            )
        }

        return ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(systemMessage) + chatMessages
        )
    }
}
