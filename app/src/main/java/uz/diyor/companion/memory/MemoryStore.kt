package uz.diyor.companion.memory

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class AiraMemory(
    val id: Long,
    val content: String,
    val createdAt: Long
)

/**
 * Private on-device store. Facts are written only after the user explicitly
 * enters and saves them in MemoryActivity.
 */
class MemoryStore(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "aira_memory.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE memories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future schema migrations are added here. Never silently erase memories.
    }

    @Synchronized
    fun add(content: String): Long {
        val clean = content.trim()
        require(clean.isNotEmpty()) { "Xotira matni bo‘sh bo‘lmasin." }
        require(clean.length <= 500) { "Bitta xotira 500 belgidan oshmasin." }
        val values = ContentValues().apply {
            put("content", clean)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insertOrThrow("memories", null, values)
    }

    @Synchronized
    fun list(limit: Int = 100): List<AiraMemory> {
        val result = mutableListOf<AiraMemory>()
        readableDatabase.query(
            "memories",
            arrayOf("id", "content", "created_at"),
            null,
            null,
            null,
            null,
            "created_at DESC",
            limit.coerceIn(1, 500).toString()
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += AiraMemory(
                    id = cursor.getLong(0),
                    content = cursor.getString(1),
                    createdAt = cursor.getLong(2)
                )
            }
        }
        return result
    }

    @Synchronized
    fun delete(id: Long): Int =
        writableDatabase.delete("memories", "id = ?", arrayOf(id.toString()))

    @Synchronized
    fun clear(): Int = writableDatabase.delete("memories", null, null)

    @Synchronized
    fun promptContext(limit: Int = 20): String =
        list(limit).asReversed().joinToString("\n") { "- ${it.content}" }
}
