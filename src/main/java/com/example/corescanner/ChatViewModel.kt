
package com.example.corescanner

import android.R.attr.content
import android.R.attr.text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corescanner.remote.ApiService
import com.example.corescanner.remote.ContentBody
import com.example.corescanner.remote.PartBody
import com.example.corescanner.remote.RequestBody
import com.example.corescanner.repository.ChatRepository
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel : ViewModel() {
    // Muestra progreso en la UI (barra)
    private val _isSending = mutableStateOf(false)
    val isSending = _isSending
    private val GEMINI_API_KEY = "AIzaSyCHRPkvn9jlXWU9WnFZdhOavpM7qWyRCXY"


    private val api = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun ask(sessionId: Long, userText: String, repository: ChatRepository) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val fullPrompt = GeminiPrompt.SYSTEM_PROMPT + " Usuario: " + userText
                val request = RequestBody(
                    contents = listOf(ContentBody(parts = listOf(PartBody(fullPrompt))))
                )
                val response = api.askToGemini(GEMINI_API_KEY, request)

                if (!response.isSuccessful) {
                    val code = response.code()
                    val body = response.errorBody()?.string()
                    repository.addAssistantMessage(sessionId, "Error HTTP $code: ${body ?: ""}".take(500))
                } else {
                    val aiText = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text ?: "No pude generar una respuesta."
                    repository.addAssistantMessage(sessionId, aiText.take(500))
                }
            } catch (e: Exception) {
                repository.addAssistantMessage(sessionId, "Excepción: ${e.message ?: "desconocida"}")
            } finally {
                _isSending.value = false
            }
        }
    }
}

