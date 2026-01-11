package com.example.superfastbrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class BookmarksAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOOKMARK = 0
        private const val VIEW_TYPE_AD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is com.example.superfastbrowser.db.Bookmark) {
            VIEW_TYPE_BOOKMARK
        } else {
            VIEW_TYPE_AD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_BOOKMARK) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bookmark_item, parent, false)
            BookmarkViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.native_ad_layout, parent, false)
            AdViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_BOOKMARK) {
            (holder as BookmarkViewHolder).bind(items[position] as com.example.superfastbrowser.db.Bookmark)
        } else {
            (holder as AdViewHolder).bind(items[position] as NativeAd)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.bookmark_title)
        private val urlTextView: TextView = itemView.findViewById(R.id.bookmark_url)

        fun bind(bookmark: com.example.superfastbrowser.db.Bookmark) {
            titleTextView.text = bookmark.title
            urlTextView.text = bookmark.url
        }
    }

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adView: NativeAdView = itemView as NativeAdView
        private val headlineView: TextView = adView.findViewById(R.id.ad_headline)
        private val advertiserView: TextView = adView.findViewById(R.id.ad_advertiser)
        private val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        private val iconView: ImageView = adView.findViewById(R.id.ad_app_icon)
        private val callToActionView: Button = adView.findViewById(R.id.ad_call_to_action)

        fun bind(nativeAd: NativeAd) {
            headlineView.text = nativeAd.headline
            advertiserView.text = nativeAd.advertiser
            mediaView.mediaContent = nativeAd.mediaContent
            iconView.setImageDrawable(nativeAd.icon?.drawable)
            callToActionView.text = nativeAd.callToAction

            adView.headlineView = headlineView
            adView.advertiserView = advertiserView
            adView.mediaView = mediaView
            adView.iconView = iconView
            adView.callToActionView = callToActionView
            adView.setNativeAd(nativeAd)
        }
    }
}
