package com.example.superfastbrowser

import android.content.Context

object AdBlocker {
    init {
        System.loadLibrary("adblocker")
    }

    external fun isBlocked(domain: String): Boolean
    external fun loadBlocklist(blocklist: String)

    fun loadBlocklistFromAssets(context: Context) {
        try {
            val inputStream = context.assets.open("blocklist.txt")
            val blocklist = inputStream.bufferedReader().use { it.readText() }
            loadBlocklist(blocklist)
        } catch (e: Exception) {
            // In a real app, you would handle this error more gracefully
            e.printStackTrace()
        }
    }
}
