package com.example.superfastbrowser

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        val listView = findViewById<ListView>(R.id.downloads_list)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        val cursor: Cursor? = downloadManager.query(query)

        val downloads = mutableListOf<String>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val statusText = when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> "Success"
                    DownloadManager.STATUS_FAILED -> "Failed"
                    DownloadManager.STATUS_PENDING -> "Pending"
                    DownloadManager.STATUS_RUNNING -> "Running"
                    else -> "Unknown"
                }
                downloads.add("$title\n$statusText")
            }
            cursor.close()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            downloads
        )

        listView.adapter = adapter
    }
}
