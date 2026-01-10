package com.example.superfastbrowser

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.superfastbrowser.db.BrowserDao
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView = findViewById<ListView>(R.id.history_list)
        val browserDao = BrowserDao(this)
        val history = browserDao.getAllHistory()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            history.map {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val date = Date(it.timestamp)
                "${it.title}\n${it.url}\n${sdf.format(date)}"
            }
        )

        listView.adapter = adapter
    }
}
