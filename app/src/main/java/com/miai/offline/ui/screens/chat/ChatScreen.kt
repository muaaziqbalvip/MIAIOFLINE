package com.miai.offline.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miai.offline.inference.ResponseMode
import com.miai.offline.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modelId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val mode by viewModel.responseMode.collectAsState()
    val modelReady by viewModel.modelReady.collectAsState()
    val modelName by viewModel.currentModelName.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(modelId) {
        viewModel.loadModel(modelId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(modelName.ifBlank { "Chat" }, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (modelReady) "Offline • Ready" else "Loading model...",
                            color = if (modelReady) SuccessGreen else TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    ModeToggle(mode = mode, onToggle = { viewModel.toggleMode() })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = modelReady
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
fun ModeToggle(mode: ResponseMode, onToggle: () -> Unit) {
    val isDeep = mode == ResponseMode.DEEP_THINKING
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .background(
                if (isDeep) MiBrandGradient else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated)),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        TextButton(onClick = onToggle) {
            Text(
                if (isDeep) "🧠 Deep Thinking" else "⚡ Fast",
                color = TextPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatUiMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) MiPurple.copy(alpha = 0.85f) else SurfaceDark,
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content.ifBlank { "..." },
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Apna sawal likhein...", color = TextSecondary) },
            enabled = enabled,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = MiPink,
                unfocusedBorderColor = BorderSubtle
            )
        )
        IconButton(
            onClick = onSend,
            enabled = enabled && text.isNotBlank(),
            modifier = Modifier
                .background(MiBrandGradient, RoundedCornerShape(50))
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = TextPrimary)
        }
    }
}
