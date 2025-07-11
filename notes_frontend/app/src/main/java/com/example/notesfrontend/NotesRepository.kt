package com.example.notesfrontend

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Supabase details (do not hard-code in production; here for demo)
private const val SUPABASE_URL = "https://mzxyorlnbfdkneiezgjz.supabase.co"
private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16eHlvcmxuYmZka25laWV6Z2p6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIwNDUxMDksImV4cCI6MjA2NzYyMTEwOX0.URYpbwtC2u5ORBlUzpWPNspXMWq_cLBOKWMOgGbilyQ"

private const val NOTES_TABLE = "notes"

// PUBLIC_INTERFACE
class NotesRepository {
    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    // PUBLIC_INTERFACE
    suspend fun listNotes(): List<Note> = withContext(Dispatchers.IO) {
        try {
            val results = supabase.postgrest[NOTES_TABLE]
                .select().order("id", ascending = false).decodeList<Note>()
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    // PUBLIC_INTERFACE
    suspend fun createNote(title: String, content: String) {
        try {
            @Serializable
            data class NoteInsert(val title: String, val content: String)
            supabase.postgrest[NOTES_TABLE]
                .insert(NoteInsert(title = title, content = content))
        } catch (e: Exception) {
            // Log/handle
        }
    }

    // PUBLIC_INTERFACE
    suspend fun editNote(id: String, title: String, content: String) {
        try {
            supabase.postgrest[NOTES_TABLE]
                .update(mapOf("title" to title, "content" to content)) {
                    filter { eq("id", id) }
                }
        } catch (e: Exception) {
            // Log/handle
        }
    }

    // PUBLIC_INTERFACE
    suspend fun deleteNote(id: String) {
        try {
            supabase.postgrest[NOTES_TABLE]
                .delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            // Log/handle
        }
    }
}
