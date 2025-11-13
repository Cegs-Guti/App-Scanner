package com.example.corescanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corescanner.repository.ChatRepository
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
        topBar = { TopAppBar(title = { Text("CoreScanner") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Qué quieres escanear?", style = MaterialTheme.typography.titleLarge)



            Button(
                onClick = {
                    scope.launch {
                        val id = repository.newSession("Nueva conversación")
                        onGoNewChat(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Comenzar a escanear") }
            Button(
                onClick = onGoHistory,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ver historial") }
        }
    }
}
