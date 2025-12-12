package ro.ulbs.tiko

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A repository for managing chat history in memory, suitable for Compose state usage.
 */
class ChatHistoryRepository {

    private val _messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
    val messages: List<ChatMessage> = _messages

    /**
     * Adds a new message from the user to the history.
     */
    fun addUserMessage(text: String) {
        val message = ChatMessage(
            role = "user",
            content = text
        )
        _messages.add(message)
    }

    /**
     * Adds a new message from the assistant to the history.
     */
    fun addAssistantMessage(text: String) {
        val message = ChatMessage(
            role = "assistant",
            content = text
        )
        _messages.add(message)
    }

    /**
     * Clears all messages from the chat history.
     */
    fun clear() {
        _messages.clear()
    }
}
