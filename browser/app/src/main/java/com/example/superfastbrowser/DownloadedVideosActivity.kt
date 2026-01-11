package com.example.superfastbrowser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.superfastbrowser.db.BrowserDao

class DownloadedVideosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloaded_videos)

        val listView = findViewById<ListView>(R.id.downloaded_videos_list)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val browserDao = BrowserDao(this)
        val query = DownloadManager.Query()
        val cursor: Cursor? = downloadManager.query(query)

        val videos = mutableListOf<Pair<Long, String>>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                val mimeType = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))
                } catch (e: Exception) {
                    null
                }

                if (mimeType != null && mimeType.startsWith("video/")) {
                    videos.add(Pair(id, title))
                }
            }
            cursor.close()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            videos.map { it.second }
        )

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val videoId = videos[position].first
            val videoUri = downloadManager.getUriForDownloadedFile(videoId)

            val subtitleId = browserDao.getSubtitleDownloadId(videoId)
            val subtitleUri = subtitleId?.let { downloadManager.getUriForDownloadedFile(it) }

            val intent = Intent(this, RustVideoPlayerActivity::class.java)
            intent.putExtra("videoUri", videoUri)
            intent.putExtra("subtitleUri", subtitleUri)
            startActivity(intent)
        }
    }
}
