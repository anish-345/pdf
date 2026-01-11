package com.example.superfastbrowser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.provider.BaseColumns
import com.example.superfastbrowser.db.BrowserDao

class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        val listView = findViewById<ListView>(R.id.downloads_list)
        val downloadedVideosButton = findViewById<Button>(R.id.downloaded_videos_button)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val browserDao = BrowserDao(this)
        val query = DownloadManager.Query()
        val cursor: Cursor? = downloadManager.query(query)

        val downloads = mutableListOf<Pair<Long, String>>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val statusText = when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> "Success"
                    DownloadManager.STATUS_FAILED -> "Failed"
                    DownloadManager.STATUS_PENDING -> "Pending"
                    DownloadManager.STATUS_RUNNING -> "Running"
                    else -> "Unknown"
                }
                downloads.add(Pair(id, "$title\n$statusText"))
            }
            cursor.close()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            downloads.map { it.second }
        )

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val downloadId = downloads[position].first
            val mimeType = try {
                downloadManager.getMimeTypeForDownloadedFile(downloadId)
            } catch (e: Exception) {
                null
            }
            val uri = downloadManager.getUriForDownloadedFile(downloadId)

            if (mimeType != null && mimeType.startsWith("video/")) {
                val subtitleId = browserDao.getSubtitleDownloadId(downloadId)
                val subtitleUri = subtitleId?.let { downloadManager.getUriForDownloadedFile(it) }

                val intent = Intent(this, RustVideoPlayerActivity::class.java)
                intent.putExtra("videoUri", uri)
                intent.putExtra("subtitleUri", subtitleUri)
                startActivity(intent)
            } else if (uri != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            }
        }

        downloadedVideosButton.setOnClickListener {
            val intent = Intent(this, DownloadedVideosActivity::class.java)
            startActivity(intent)
        }
    }
}
