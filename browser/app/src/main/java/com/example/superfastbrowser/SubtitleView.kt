package com.example.superfastbrowser

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.TextView
import android.widget.VideoView

class SubtitleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private var subtitles: List<Subtitle> = emptyList()
    private var videoView: VideoView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    fun setSubtitles(subtitles: List<Subtitle>) {
        this.subtitles = subtitles
    }

    fun setVideoView(videoView: VideoView) {
        this.videoView = videoView
        startUpdating()
    }

    private fun startUpdating() {
        updateRunnable = Runnable {
            val currentPosition = videoView?.currentPosition?.toLong() ?: 0
            val subtitle = subtitles.find { currentPosition in it.startTime..it.endTime }
            text = subtitle?.text ?: ""
            handler.postDelayed(updateRunnable!!, 100)
        }
        handler.post(updateRunnable!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateRunnable?.let { handler.removeCallbacks(it) }
    }
}
