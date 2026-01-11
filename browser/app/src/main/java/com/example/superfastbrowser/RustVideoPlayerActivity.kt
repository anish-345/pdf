package com.example.superfastbrowser

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class RustVideoPlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private var playerPtr: Long = 0
    private lateinit var subtitleView: SubtitleView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rust_video_player)

        System.loadLibrary("rust_video_player")
        playerPtr = init()

        subtitleView = findViewById(R.id.subtitle_view)
        val surfaceView = findViewById<SurfaceView>(R.id.surface_view)
        surfaceView.holder.addCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        release(playerPtr)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setSurface(playerPtr, holder.surface)
        val videoUri = intent.getParcelableExtra<Uri>("videoUri")
        val subtitleUri = intent.getParcelableExtra<Uri>("subtitleUri")

        if (videoUri != null) {
            val path = videoUri.path
            if (path != null) {
                loadMedia(playerPtr, path)
                if (subtitleUri != null) {
                    val subtitlePath = subtitleUri.path
                    if (subtitlePath != null) {
                        addSubtitle(playerPtr, subtitlePath)
                    }
                    val inputStream: InputStream? = contentResolver.openInputStream(subtitleUri)
                    if (inputStream != null) {
                        val subtitles = VttParser().parse(inputStream)
                        subtitleView.setSubtitles(subtitles)
                        updateTimeRunnable = Runnable {
                            val currentTime = getTime(playerPtr)
                            subtitleView.update(currentTime)
                            handler.postDelayed(updateTimeRunnable, 100)
                        }
                        handler.post(updateTimeRunnable)
                    }
                }
                play(playerPtr)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private external fun init(): Long
    private external fun setSurface(playerPtr: Long, surface: Any)
    private external fun loadMedia(playerPtr: Long, mediaPath: String)
    private external fun addSubtitle(playerPtr: Long, subtitlePath: String)
    private external fun play(playerPtr: Long)
    private external fun pause(playerPtr: Long)
    private external fun release(playerPtr: Long)
    private external fun getTime(playerPtr: Long): Long
}
