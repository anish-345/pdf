package com.example.superfastbrowser

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val videoView = findViewById<VideoView>(R.id.video_view)
        val subtitleView = findViewById<SubtitleView>(R.id.subtitle_view)
        val videoUri = intent.getParcelableExtra<Uri>("videoUri")
        val subtitleUri = intent.getParcelableExtra<Uri>("subtitleUri")

        if (videoUri != null) {
            videoView.setVideoURI(videoUri)
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            if (subtitleUri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(subtitleUri)
                if (inputStream != null) {
                    val subtitles = VttParser().parse(inputStream)
                    subtitleView.setSubtitles(subtitles)
                    subtitleView.setVideoView(videoView)
                }
            }

            val playbackSpeeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
            var currentSpeedIndex = 0
            val playbackSpeedButton = findViewById<Button>(R.id.playback_speed)
            playbackSpeedButton.setOnClickListener {
                currentSpeedIndex = (currentSpeedIndex + 1) % playbackSpeeds.size
                val newSpeed = playbackSpeeds[currentSpeedIndex]
                videoView.playbackParams = videoView.playbackParams.setSpeed(newSpeed)
                playbackSpeedButton.text = "${newSpeed}x"
            }

            videoView.start()
        }
    }
}
