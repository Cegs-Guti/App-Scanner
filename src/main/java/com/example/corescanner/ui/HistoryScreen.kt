package com.example.corescanner.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    onBack: () -> Unit          // ✅ NUEVO PARÁMETRO
) {
    val sessions by repository.sessions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // --- Estado para renombrar ---
    var renameDialogSessionId by remember { mutableStateOf<Long?>(null) }
    var renameText by remember { mutableStateOf("") }

    // --- Estado para selección múltiple y borrado ---
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectionMode) {
                        Text("${selectedIds.size} seleccionados")
                    } else {
                        Text("Historial de conversación")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) {
                                // Si está en modo selección, primero la cancelamos
                                selectionMode = false
                                selectedIds = emptySet()
                            } else {
                                // Si no, volvemos atrás como en ChatScreen
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar conversación(es)"
                            )
                        }
                    }
                }
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
                    }
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
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        val date = java.text.SimpleDateFormat("dd MMM HH:mm")
                            .format(java.util.Date(s.updatedAt))
                        Text(date)
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
                                    contentDescription = "Renombrar conversación"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor =
                            if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                                    if (selectedIds.isEmpty()) {
                                        selectionMode = false
                                    }
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
                Divider()
            }
        }
    }

    // --- Diálogo renombrar ---
    if (renameDialogSessionId != null) {
        AlertDialog(
            onDismissRequest = { renameDialogSessionId = null },
            title = { Text("Renombrar conversación") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Nombre de la conversación") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = renameDialogSessionId
                        if (id != null && renameText.isNotBlank()) {
                            scope.launch {
                                repository.renameSession(id, renameText.trim())
                            }
                        }
                        renameDialogSessionId = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogSessionId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Diálogo eliminar múltiples ---
    if (showDeleteDialog && selectedIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text("Eliminar conversaciones") },
            text = {
                Text(
                    "¿Seguro que deseas eliminar ${selectedIds.size} conversación(es)? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteSessions(selectedIds.toList())
                        }
                        showDeleteDialog = false
                        selectionMode = false
                        selectedIds = emptySet()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}