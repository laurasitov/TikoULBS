package ro.ulbs.tiko

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single message in the chat, used for both UI and serialization.
 */
@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for sending a chat completion request to the OpenAI API.
 */
data class ChatRequest(
    val model: String,
    val messages: List<MessagePayload>
)

/**
 * Represents a message payload within the [ChatRequest].
 */
data class MessagePayload(
    val role: String,
    val content: String
)

/**
 * Data class representing the response from the OpenAI chat completion API.
 */
data class ChatResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    @SerializedName("message")
    val messagePayload: MessagePayload,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)
