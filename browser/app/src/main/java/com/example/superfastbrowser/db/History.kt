package com.example.superfastbrowser.db

data class History(
    val id: Long,
    val title: String,
    val url: String,
    val timestamp: Long
)
