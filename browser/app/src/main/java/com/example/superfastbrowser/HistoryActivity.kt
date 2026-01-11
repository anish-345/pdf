package com.example.superfastbrowser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.superfastbrowser.db.BrowserDao
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

class HistoryActivity : AppCompatActivity() {

    private val items = mutableListOf<Any>()
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView = findViewById<RecyclerView>(R.id.history_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(items)
        recyclerView.adapter = adapter

        val browserDao = BrowserDao(this)
        val history = browserDao.getAllHistory()
        items.addAll(history)

        loadNativeAds()
    }

    private fun loadNativeAds() {
        val adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->
                insertAd(nativeAd)
            }
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 5)
    }

    private fun insertAd(nativeAd: NativeAd) {
        if (items.isEmpty()) {
            return
        }
        val adIndex = if (items.size > 4) 4 else items.size -1
        items.add(adIndex, nativeAd)
        adapter.notifyItemInserted(adIndex)
    }
}
