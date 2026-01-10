package com.example.superfastbrowser

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.superfastbrowser.db.BrowserDao

class BookmarksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        val listView = findViewById<ListView>(R.id.bookmarks_list)
        val browserDao = BrowserDao(this)
        val bookmarks = browserDao.getAllBookmarks()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            bookmarks.map { "${it.title}\n${it.url}" }
        )

        listView.adapter = adapter
    }
}
