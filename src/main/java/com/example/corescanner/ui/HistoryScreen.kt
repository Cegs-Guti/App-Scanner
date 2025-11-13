package com.example.corescanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.corescanner.repository.ChatRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: ChatRepository,
    onOpen: (Long) -> Unit,
    onNew: (Long) -> Unit
) {
    val sessions by repository.sessions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Historial de conversación") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val id = repository.newSession("Nueva conversación")
                        onNew(id)
                    }
                }
            ) { Text("Nueva conversación") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(sessions) { s ->
                ListItem(
                    headlineContent = {
                        Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        val date = java.text.SimpleDateFormat("dd MMM HH:mm")
                            .format(java.util.Date(s.updatedAt))
                        Text(date)
                    },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable { onOpen(s.id) }
                )
                Divider()
            }
        }
    }
}

