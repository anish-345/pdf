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
    external fun setWhitelist(whitelist: String)
    external fun analyzePagePrivacy(html: String): Int

    fun loadBlocklistFromAssets(context: Context) {
        try {
            val inputStream = context.assets.open("blocklist.txt")
            val blocklist = inputStream.bufferedReader().use { it.readText() }
            loadBlocklist(blocklist)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.error_loading_blocklist, Toast.LENGTH_SHORT).show()
        }
    }
}
