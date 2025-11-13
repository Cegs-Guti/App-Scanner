
package com.example.corescanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corescanner.repository.ChatRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueChatScreen(
    sessionId: Long,
    repository: ChatRepository,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val messages by repository.messages(sessionId).collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retomar conversación") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("CONTINUAR CHAT") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messages) { m ->
                Text("${if (m.role == "user") "Tú" else "Asistente"}: ${m.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

