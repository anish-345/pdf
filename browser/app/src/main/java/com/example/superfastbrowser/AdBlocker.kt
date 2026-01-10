package com.example.superfastbrowser

import android.content.Context
import android.widget.Toast

object AdBlocker {
    init {
        System.loadLibrary("adblocker")
    }

    external fun isBlocked(domain: String): Boolean
    external fun loadBlocklist(blocklist: String)
    external fun sanitizeUrl(url: String): String

    fun loadBlocklistFromAssets(context: Context) {
        try {
            val inputStream = context.assets.open("blocklist.txt")
            val blocklist = inputStream.bufferedReader().use { it.readText() }
            loadBlocklist(blocklist)
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading blocklist", Toast.LENGTH_SHORT).show()
        }
    }
}
