package com.example.superfastbrowser

import android.app.Service
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PlaybackService : Service() {

    private val binder = LocalBinder()
    private var playerPtr: Long = 0

    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("rust_video_player")
        playerPtr = init()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "PLAYBACK_CHANNEL",
                "Playback",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        release(playerPtr)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, RustVideoPlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, "PLAYBACK_CHANNEL")
            .setContentTitle("Video Playing")
            .setContentText("Tap to return to the video")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    fun setSurface(surface: Any) {
        setSurface(playerPtr, surface)
    }

    fun loadMedia(mediaPath: String, subtitlePath: String?) {
        loadMedia(playerPtr, mediaPath)
        if (subtitlePath != null) {
            addSubtitle(playerPtr, subtitlePath)
        }
    }

    fun play() {
        play(playerPtr)
    }

    fun pause() {
        pause(playerPtr)
    }

    fun getTime(): Long {
        return getTime(playerPtr)
    }

    private external fun init(): Long
    private external fun setSurface(playerPtr: Long, surface: Any)
    private external fun loadMedia(playerPtr: Long, mediaPath: String)
    private external fun addSubtitle(playerPtr: Long, subtitlePath: String)
    private external fun play(playerPtr: Long)
    private external fun pause(playerPtr: Long)
    private external fun release(playerPtr: Long)
    private external fun getTime(playerPtr: Long): Long
}
