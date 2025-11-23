package com.example.corescanner.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.corescanner.R
import com.example.corescanner.repository.ChatRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    repository: ChatRepository,
    onOpen: (Long) -> Unit,
    onNew: (Long) -> Unit,
    onBack: () -> Unit
) {
    val sessions by repository.sessions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var renameDialogSessionId by remember { mutableStateOf<Long?>(null) }
    var renameText by remember { mutableStateOf("") }

    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionMode)
                            "${selectedIds.size} seleccionados"
                        else
                            "Historial de conversación",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) {
                                selectionMode = false
                                selectedIds = emptySet()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar conversación(es)",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        floatingActionButton = {
            if (!selectionMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            val id = repository.newSession("Nueva conversación")
                            onNew(id)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("Nueva conversación")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(sessions) { s ->
                val isSelected = selectedIds.contains(s.id)

                ListItem(
                    headlineContent = {
                        Text(
                            s.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = {
                        val date = java.text.SimpleDateFormat("dd MMM HH:mm")
                            .format(java.util.Date(s.updatedAt))
                        Text(
                            date,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (!selectionMode) {
                            IconButton(
                                onClick = {
                                    renameDialogSessionId = s.id
                                    renameText = s.title
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rename_icon),
                                    contentDescription = "Renombrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor =
                            if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                            else
                                MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                if (selectionMode) {
                                    selectedIds =
                                        if (isSelected) selectedIds - s.id
                                        else selectedIds + s.id
                                    if (selectedIds.isEmpty()) selectionMode = false
                                } else {
                                    onOpen(s.id)
                                }
                            },
                            onLongClick = {
                                selectionMode = true
                                selectedIds =
                                    if (isSelected) selectedIds - s.id
                                    else selectedIds + s.id
                            }
                        )
                )

                Divider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }

    // --- Diálogo: Renombrar conversación ---
    if (renameDialogSessionId != null) {
        AlertDialog(
            onDismissRequest = { renameDialogSessionId = null },
            title = {
                Text(
                    "Renombrar conversación",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Nombre", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = renameDialogSessionId
                    if (id != null && renameText.isNotBlank()) {
                        scope.launch {
                            repository.renameSession(id, renameText.trim())
                        }
                    }
                    renameDialogSessionId = null
                }) {
                    Text("Guardar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogSessionId = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // --- Diálogo: Eliminar múltiples ---
    if (showDeleteDialog && selectedIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar conversaciones", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "¿Seguro que deseas eliminar ${selectedIds.size} conversación(es)?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.deleteSessions(selectedIds.toList()) }
                    showDeleteDialog = false
                    selectionMode = false
                    selectedIds = emptySet()
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}