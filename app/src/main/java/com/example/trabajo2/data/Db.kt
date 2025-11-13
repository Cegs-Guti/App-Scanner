
package com.example.trabajo2.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [ForeignKey(
        entity = ChatSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,     // "user" | "assistant"
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Insert suspend fun insertSession(s: ChatSession): Long
    @Update suspend fun updateSession(s: ChatSession)
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun observeSessions(): kotlinx.coroutines.flow.Flow<List<ChatSession>>
    @Query("SELECT * FROM chat_sessions WHERE id=:id")
    suspend fun getSession(id: Long): ChatSession?

    @Insert suspend fun insertMessage(m: ChatMessage): Long
    @Query("SELECT * FROM chat_messages WHERE sessionId=:sessionId ORDER BY createdAt ASC")
    fun observeMessages(sessionId: Long): kotlinx.coroutines.flow.Flow<List<ChatMessage>>
    @Query("SELECT text FROM chat_messages WHERE sessionId=:sessionId ORDER BY createdAt ASC LIMIT 1")
    suspend fun firstMessageText(sessionId: Long): String?
}

@Database(entities = [ChatSession::class, ChatMessage::class], version = 1)
abstract class ChatDb : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var INSTANCE: ChatDb? = null
        fun get(context: Context): ChatDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChatDb::class.java,
                    "chat.db"
                ).build().also { INSTANCE = it }
            }
    }
}

