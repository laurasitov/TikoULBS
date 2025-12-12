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

                response.choices.firstOrNull()?.messagePayload?.content?.let { assistantText ->
                    repository.addAssistantMessage(assistantText)
                } ?: run {
                    handleApiError("The response from the assistant was empty.")
                }

            } catch (e: UnknownHostException) {
                handleApiError("I couldn't connect to the internet. Please check your connection and try again.")
            } catch (e: HttpException) {
                handleApiError("I'm having trouble connecting to my services right now. Please try again in a moment.")
            } catch (e: Exception) {
                handleApiError("An unexpected error occurred. Please try again.")
            }

            // Always reset loading state
            _uiState.update { it.copy(messages = repository.messages, isLoading = false) }
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
