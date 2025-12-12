package ro.ulbs.tiko

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
