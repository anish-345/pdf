package com.example.superfastbrowser.db

data class Password(
    val id: Long,
    val url: String,
    val username: String,
    val passwordEncrypted: ByteArray,
    val iv: ByteArray
)
