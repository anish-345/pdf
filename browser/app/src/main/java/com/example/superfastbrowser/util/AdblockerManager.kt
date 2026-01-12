package com.example.superfastbrowser.util

import android.content.Context
import com.example.superfastbrowser.AdBlocker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class AdblockerManager(private val context: Context) {

    private val admobDomains = listOf(
        "googleads.g.doubleclick.net",
        "adservice.google.com",
        "admob.google.com",
        "www.googleadservices.com",
        "pagead2.googlesyndication.com"
    )

    init {
        AdBlocker.setWhitelist(admobDomains.joinToString("\n"))
    }

    suspend fun downloadAndStoreList(url: String, filename: String) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, filename)
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }
    }

    fun mergeAndLoadRules(popularLists: List<String>, customRules: String) {
        val mergedRules = StringBuilder()
        popularLists.forEach { filename ->
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                mergedRules.append(file.readText())
                mergedRules.append("\n")
            }
        }
        mergedRules.append(customRules)
        AdBlocker.loadBlocklist(mergedRules.toString())
    }

    fun setWhitelist(whitelist: String) {
        AdBlocker.setWhitelist(whitelist)
    }
}
