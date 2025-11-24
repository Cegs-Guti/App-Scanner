package com.example.corescanner

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corescanner.remote.ApiService
import com.example.corescanner.remote.ContentBody
import com.example.corescanner.remote.InlineData
import com.example.corescanner.remote.PartBody
import com.example.corescanner.remote.RequestBody
import com.example.corescanner.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ChatViewModel : ViewModel() {

    private val _isSending = mutableStateOf(false)
    val isSending = _isSending

    private val GEMINI_API_KEY = "AIzaSyCHRPkvn9jlXWU9WnFZdhOavpM7qWyRCXY"

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    private var lastImageDescription: String? = null

    fun ask(sessionId: Long, userText: String, repository: ChatRepository) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val fullPrompt = buildString {
                    append(GeminiPrompt.SYSTEM_PROMPT)
                    append("\n\n")
                    if (lastImageDescription != null) {
                        append("Análisis previo de la última imagen enviada por el usuario:\n")
                        append(lastImageDescription)
                        append("\n\n")
                    }
                    append("Pregunta del usuario: ")
                    append(userText)
                }

                val request = RequestBody(
                    contents = listOf(
                        ContentBody(
                            parts = listOf(
                                PartBody(text = fullPrompt)
                            )
                        )
                    )
                )

                val response = api.askToGemini(GEMINI_API_KEY, request)

                if (!response.isSuccessful) {
                    val code = response.code()
                    val body = response.errorBody()?.string()
                    repository.addAssistantMessage(
                        sessionId,
                        "Error HTTP $code: ${body ?: ""}".take(500)
                    )
                } else {
                    val aiText = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text ?: "No pude generar una respuesta."
                    repository.addAssistantMessage(sessionId, aiText)
                }
            } catch (e: Exception) {
                repository.addAssistantMessage(
                    sessionId,
                    "Excepción: ${e.message ?: "desconocida"}"
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    private suspend fun bitmapToBase64(bitmap: Bitmap): String =
        withContext(Dispatchers.Default) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        }

    private suspend fun uriToBase64(uri: Uri, repository: ChatRepository): String? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = repository.context.contentResolver
                resolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                null
            }
        }

    fun askWithImageFromBitmap(
        sessionId: Long,
        bitmap: Bitmap,
        repository: ChatRepository
    ) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val base64 = bitmapToBase64(bitmap)

                val request = RequestBody(
                    contents = listOf(
                        ContentBody(
                            parts = listOf(
                                PartBody(
                                    text = GeminiPrompt.SYSTEM_PROMPT +
                                            "\n\nAnaliza esta imagen capturada por el usuario y describe qué se observa."
                                ),
                                PartBody(
                                    inlineData = InlineData(
                                        mimeType = "image/jpeg",
                                        data = base64
                                    )
                                )
                            )
                        )
                    )
                )

                val response = api.askToGemini(GEMINI_API_KEY, request)

                if (!response.isSuccessful) {
                    repository.addAssistantMessage(
                        sessionId,
                        "Error imagen HTTP ${response.code()}"
                    )
                } else {
                    val aiText = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text ?: "No pude analizar la imagen."

                    lastImageDescription = aiText

                    repository.addAssistantMessage(sessionId, aiText)
                }

            } catch (e: Exception) {
                repository.addAssistantMessage(
                    sessionId,
                    "Error al procesar imagen: ${e.message}"
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun resetLastImage() {
        lastImageDescription = null
    }

    fun askWithImageFromUri(
        sessionId: Long,
        uri: Uri,
        repository: ChatRepository
    ) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val base64 = uriToBase64(uri, repository)

                if (base64 == null) {
                    repository.addAssistantMessage(sessionId, "No pude leer la imagen seleccionada.")
                    _isSending.value = false
                    return@launch
                }

                val request = RequestBody(
                    contents = listOf(
                        ContentBody(
                            parts = listOf(
                                PartBody(
                                    text = GeminiPrompt.SYSTEM_PROMPT +
                                            "\n\nAnaliza esta imagen seleccionada desde la galería y describe qué se observa."
                                ),
                                PartBody(
                                    inlineData = InlineData(
                                        mimeType = "image/jpeg",
                                        data = base64
                                    )
                                )
                            )
                        )
                    )
                )

                val response = api.askToGemini(GEMINI_API_KEY, request)

                if (!response.isSuccessful) {
                    repository.addAssistantMessage(
                        sessionId,
                        "Error imagen HTTP ${response.code()}"
                    )
                } else {
                    val aiText = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text ?: "No pude analizar la imagen."
                    lastImageDescription = aiText

                    repository.addAssistantMessage(sessionId, aiText)
                }

            } catch (e: Exception) {
                repository.addAssistantMessage(
                    sessionId,
                    "Error al procesar imagen: ${e.message}"
                )
            } finally {
                _isSending.value = false
            }
        }
    }
}