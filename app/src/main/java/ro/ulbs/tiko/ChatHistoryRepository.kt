package ro.ulbs.tiko

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

class ChatHistoryRepository(private val context: Context) {

    private val messagesKey = stringPreferencesKey("chat_messages")

    val chatHistory: Flow<List<ChatMessage>> = context.dataStore.data
        .map {
            val jsonString = it[messagesKey]
            if (jsonString != null) {
                Json.decodeFromString<List<ChatMessage>>(jsonString)
            } else {
                emptyList()
            }
        }

    suspend fun saveChatHistory(messages: List<ChatMessage>) {
        context.dataStore.edit {
            it[messagesKey] = Json.encodeToString(messages)
        }
    }
}