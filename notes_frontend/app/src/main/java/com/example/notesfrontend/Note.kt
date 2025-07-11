package com.example.notesfrontend

import kotlinx.serialization.Serializable

/**
 * Simple Note data class.
 */
@Serializable
data class Note(
    val id: String, // Supabase UUID
    val title: String,
    val content: String
)
