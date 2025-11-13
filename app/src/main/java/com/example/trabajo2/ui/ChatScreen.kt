package com.example.trabajo2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trabajo2.ChatViewModel
import com.example.trabajo2.repository.ChatRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: Long,
    repository: ChatRepository,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val isSending by vm.isSending
    val messages by repository.messages(sessionId).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente IA") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                if (isSending) LinearProgressIndicator(Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribir…") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val t = text.trim()
                            if (t.isNotEmpty() && !isSending) {
                                scope.launch {
                                    repository.addUserMessage(sessionId, t)  // ✅ suspend correcto
                                    text = ""
                                    vm.ask(sessionId, t, repository)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messages) { m ->
                val isUser = m.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .widthIn(max = 320.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Text(m.text, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}
