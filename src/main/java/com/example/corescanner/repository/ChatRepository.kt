
package com.example.corescanner.repository

import com.example.corescanner.data.ChatDao
import com.example.corescanner.data.ChatMessage
import com.example.corescanner.data.ChatSession
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatDao) {
    suspend fun newSession(initialTitle: String = "Nueva conversación"): Long =
        dao.insertSession(ChatSession(title = initialTitle))

    fun sessions(): Flow<List<ChatSession>> = dao.observeSessions()
    fun messages(sessionId: Long) = dao.observeMessages(sessionId)

    suspend fun addUserMessage(sessionId: Long, text: String) {
        dao.insertMessage(ChatMessage(sessionId = sessionId, role = "user", text = text))
        dao.getSession(sessionId)?.let { dao.updateSession(it.copy(updatedAt = System.currentTimeMillis())) }
    }

    suspend fun addAssistantMessage(sessionId: Long, text: String) {
        dao.insertMessage(ChatMessage(sessionId = sessionId, role = "assistant", text = text))
        dao.getSession(sessionId)?.let { dao.updateSession(it.copy(updatedAt = System.currentTimeMillis())) }
    }

    suspend fun firstMessage(sessionId: Long): String? = dao.firstMessageText(sessionId)
}

