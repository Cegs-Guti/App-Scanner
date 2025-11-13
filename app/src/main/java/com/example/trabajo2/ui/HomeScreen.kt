package com.example.trabajo2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trabajo2.repository.ChatRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ChatRepository,
    onGoHistory: () -> Unit,
    onGoNewChat: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Asistente IA") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Qué quieres hacer?", style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = onGoHistory,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ver historial") }

            Button(
                onClick = {
                    scope.launch {
                        val id = repository.newSession("Nueva conversación")
                        onGoNewChat(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Nueva conversación") }
        }
    }
}
