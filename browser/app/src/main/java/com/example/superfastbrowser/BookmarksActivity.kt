package com.example.superfastbrowser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.superfastbrowser.db.BrowserDao
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

class BookmarksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookmarksAdapter
    private val listItems = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        recyclerView = findViewById(R.id.bookmarks_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val browserDao = BrowserDao(this)
        listItems.addAll(browserDao.getAllBookmarks())

        adapter = BookmarksAdapter(listItems)
        recyclerView.adapter = adapter

        loadNativeAds()
    }

    private fun loadNativeAds() {
        val adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                // Add the ad to the list
                if (listItems.size > 2) {
                    listItems.add(2, ad)
                    adapter.notifyItemInserted(2)
                }
            }
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 1)
    }
}
