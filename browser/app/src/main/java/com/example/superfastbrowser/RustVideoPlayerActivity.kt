package com.example.superfastbrowser

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class RustVideoPlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var subtitleView: SubtitleView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private var playbackService: PlaybackService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlaybackService.LocalBinder
            playbackService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rust_video_player)

        subtitleView = findViewById(R.id.subtitle_view)
        val surfaceView = findViewById<SurfaceView>(R.id.surface_view)
        surfaceView.holder.addCallback(this)

        Intent(this, PlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        playbackService?.setSurface(holder.surface)
        val videoUri = intent.getParcelableExtra<Uri>("videoUri")
        val subtitleUri = intent.getParcelableExtra<Uri>("subtitleUri")

        if (videoUri != null) {
            val path = videoUri.path
            if (path != null) {
                playbackService?.loadMedia(path, subtitleUri?.path)
                if (subtitleUri != null) {
                    val inputStream: InputStream? = contentResolver.openInputStream(subtitleUri)
                    if (inputStream != null) {
                        val subtitles = VttParser().parse(inputStream)
                        subtitleView.setSubtitles(subtitles)
                        updateTimeRunnable = Runnable {
                            playbackService?.getTime()?.let {
                                subtitleView.update(it)
                            }
                            handler.postDelayed(updateTimeRunnable, 100)
                        }
                        handler.post(updateTimeRunnable)
                    }
                }
                playbackService?.play()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onStop() {
        super.onStop()
        val backgroundPlaybackSwitch = findViewById<android.widget.Switch>(R.id.background_playback_switch)
        if (!backgroundPlaybackSwitch.isChecked) {
            playbackService?.pause()
            stopService(Intent(this, PlaybackService::class.java))
        }
    }
}
