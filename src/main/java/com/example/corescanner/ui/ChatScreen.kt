package com.example.corescanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.corescanner.ChatViewModel
import com.example.corescanner.R
import com.example.corescanner.repository.ChatRepository

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

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

    var fotoTomada by remember { mutableStateOf<Bitmap?>(null) }
    var imagenGaleria by remember { mutableStateOf<Uri?>(null) }
    var imagenGaleriaBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(imagenGaleria) {
        imagenGaleriaBitmap = imagenGaleria?.let { uri ->
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    val listState = rememberLazyListState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoTomada = bitmap

            scope.launch {
                repository.addUserMessage(
                    sessionId,
                    "[Imagen enviada desde la cámara]"
                )
                vm.askWithImageFromBitmap(sessionId, bitmap, repository)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenGaleria = uri
            scope.launch {
                repository.addUserMessage(
                    sessionId,
                    "[Imagen seleccionada desde la galería]"
                )
                vm.askWithImageFromUri(sessionId, uri, repository)
            }
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        tienePermisoCamara = granted
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente IA") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            // ⭐ imePadding() + navigationBarsPadding()
            Column(
                Modifier
                    .fillMaxWidth()
                    .imePadding()              // mueve la barra cuando aparece el teclado
                    .navigationBarsPadding()   // evita solaparse con la barra de navegación
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // Botón Cámara
                    IconButton(
                        onClick = {
                            if (tienePermisoCamara) {
                                cameraLauncher.launch(null)
                            } else {
                                requestCameraPermissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camara_icon),
                            contentDescription = "Cámara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón Galería
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.galery_icon),
                            contentDescription = "Galería",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
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
                                    repository.addUserMessage(sessionId, t)
                                    text = ""
                                    vm.ask(sessionId, t, repository)

                                    // ⭐ opcional: autoscroll al enviar
                                    listState.animateScrollToItem(messages.size.coerceAtLeast(0))
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
            state = listState,
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
                        when {
                            isUser && m.text == "[Imagen enviada desde la cámara]" && fotoTomada != null -> {
                                Image(
                                    bitmap = fotoTomada!!.asImageBitmap(),
                                    contentDescription = "Foto enviada",
                                    modifier = Modifier
                                        .sizeIn(maxWidth = 260.dp, maxHeight = 260.dp)
                                        .padding(6.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            isUser && m.text == "[Imagen seleccionada desde la galería]" && imagenGaleriaBitmap != null -> {
                                Image(
                                    bitmap = imagenGaleriaBitmap!!.asImageBitmap(),
                                    contentDescription = "Imagen enviada",
                                    modifier = Modifier
                                        .sizeIn(maxWidth = 260.dp, maxHeight = 260.dp)
                                        .padding(6.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            else -> {
                                Text(
                                    m.text,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}