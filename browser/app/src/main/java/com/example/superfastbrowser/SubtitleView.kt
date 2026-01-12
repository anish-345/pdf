package com.example.superfastbrowser

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class SubtitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var subtitles: List<VttParser.Subtitle>? = null

    fun setSubtitles(subtitles: List<VttParser.Subtitle>) {
        this.subtitles = subtitles
    }

    fun update(timeInMillis: Long) {
        val subtitle = subtitles?.find { timeInMillis in it.startTime..it.endTime }
        text = subtitle?.text ?: ""
    }
}
