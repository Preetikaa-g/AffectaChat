package com.example.affectachat.model

data class TempJournalEntry(
    val id: Long,
    val title: String = "",
    val content: String, // Text or file path
    val emotion: com.example.affectachat.Emotion,
    val isAudio: Boolean = false
)
