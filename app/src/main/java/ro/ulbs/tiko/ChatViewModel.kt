package ro.ulbs.tiko

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException

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
        if (repository.messages.isEmpty()) {
            val welcomeMessage = "Hello! I'm Tiko, your ULBS student assistant. You can ask me about the academic calendar, scholarships, faculties, and other university procedures. How can I help you today?"
            repository.addAssistantMessage(welcomeMessage)
        }
        _uiState.update { it.copy(messages = repository.messages) }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) {
            return // Ignore empty input
        }

        repository.addUserMessage(userText)
        _uiState.update {
            it.copy(
                messages = repository.messages,
                isLoading = true,
                error = null
            )
        }

        if (isQuestionOutOfScope(userText)) {
            repository.addAssistantMessage(OUT_OF_SCOPE_RESPONSE)
            _uiState.update { it.copy(messages = repository.messages, isLoading = false) }
            return
        }

        viewModelScope.launch {
            try {
                val request = buildChatRequest(repository.messages)
                val response = openAIService.getChatCompletion(request)

                val assistantText = response.choices.firstOrNull()?.messagePayload?.content
                if (!assistantText.isNullOrBlank()) {
                    repository.addAssistantMessage(assistantText)
                } else {
                    handleApiError("I'm sorry, but I couldn't generate a response. Please try again.")
                }

            } catch (e: UnknownHostException) {
                handleApiError("I couldn't connect to the internet. Please check your connection and try again.")
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    handleApiError("I'm currently receiving a lot of requests. Please try again in a few moments.")
                } else {
                    handleApiError("I'm having trouble connecting to my services right now. Please try again in a moment.")
                }
            } catch (e: Exception) {
                handleApiError("An unexpected error occurred. Please try again.")
            } finally {
                // Always reset loading state to ensure the UI is usable
                _uiState.update { it.copy(messages = repository.messages, isLoading = false) }
            }
        }
    }

    private fun handleApiError(errorMessage: String) {
        repository.addAssistantMessage(errorMessage)
    }

    private fun buildChatRequest(history: List<ChatMessage>): ChatRequest {
        val systemMessage = MessagePayload(role = "system", content = TIKO_SYSTEM_PROMPT)
        val chatMessages = history.map { message ->
            MessagePayload(role = message.role, content = message.content)
        }
        return ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(systemMessage) + chatMessages
        )
    }
}
