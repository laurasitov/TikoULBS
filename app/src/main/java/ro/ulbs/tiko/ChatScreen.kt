package ro.ulbs.tiko

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DateFormat
import java.util.Date

private val USER_BUBBLE_SHAPE = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
private val ASSISTANT_BUBBLE_SHAPE = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    val uiState by chatViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.error?.let {
        LaunchedEffect(it) {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            MessageList(messages = uiState.messages, isLoading = uiState.isLoading)
            MessageInput(
                onSendMessage = { text -> chatViewModel.sendMessage(text) },
                isLoading = uiState.isLoading
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageList(messages: List<ChatMessage>, isLoading: Boolean) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        val targetIndex = if (isLoading) messages.size else messages.size - 1
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            ChatBubble(
                message = message,
                modifier = Modifier.animateItemPlacement(
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            )
        }
        if (isLoading) {
            item { TypingIndicator() }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alphas = (1..3).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, delayMillis = index * 150, easing = LinearEasing),
            ), label = ""
        )
    }

    Row(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = ASSISTANT_BUBBLE_SHAPE, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                alphas.forEach {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .alpha(it.value)
                            .background(MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val bubbleShape = if (isUser) USER_BUBBLE_SHAPE else ASSISTANT_BUBBLE_SHAPE
    val horizontalPadding = if (isUser) PaddingValues(start = 64.dp, end = 16.dp) else PaddingValues(start = 16.dp, end = 64.dp)

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontalPadding),
        horizontalAlignment = alignment
    ) {
        Surface(shape = bubbleShape, color = bubbleColor) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, LocalContext.current.resources.configuration.locales[0])
    return dateFormat.format(Date(timestamp))
}

@Composable
fun MessageInput(onSendMessage: (String) -> Unit, isLoading: Boolean) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Ask Tiko...") },
            modifier = Modifier.weight(1f),
            maxLines = 3,
            enabled = !isLoading
        )
        IconButton(
            onClick = { if (text.isNotBlank()) { onSendMessage(text); text = "" } },
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Send message")
        }
    }
}
