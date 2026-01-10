package com.example.superfastbrowser

import android.util.Log
import java.io.InputStream
import java.util.Scanner

data class Subtitle(val startTime: Long, val endTime: Long, val text: String)

class VttParser {

    fun parse(inputStream: InputStream): List<Subtitle> {
        val subtitles = mutableListOf<Subtitle>()
        Scanner(inputStream).use { scanner ->
            // Skip the first line (WEBVTT)
            if (scanner.hasNextLine()) {
                scanner.nextLine()
            }

            while (scanner.hasNextLine()) {
                val timeLine = scanner.nextLine()
                try {
                    if (timeLine.contains("-->")) {
                        val times = timeLine.split(" --> ")
                        if (times.size == 2) {
                            val startTime = parseTimeToMillis(times[0])
                            val endTime = parseTimeToMillis(times[1])
                            if (scanner.hasNextLine()) {
                                val text = scanner.nextLine()
                                if (startTime != -1L && endTime != -1L) {
                                    subtitles.add(Subtitle(startTime, endTime, text))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VttParser", "Error parsing line: $timeLine", e)
                }
            }
        }
        return subtitles
    }

    private fun parseTimeToMillis(time: String): Long {
        return try {
            val parts = time.trim().split(":")
            val hours: Long
            val minutes: Long
            val secondsAndMillis: List<String>

            if (parts.size == 3) {
                hours = parts[0].toLong()
                minutes = parts[1].toLong()
                secondsAndMillis = parts[2].split(".")
            } else if (parts.size == 2) {
                hours = 0
                minutes = parts[0].toLong()
                secondsAndMillis = parts[1].split(".")
            } else {
                return -1L
            }

            val seconds = secondsAndMillis[0].toLong()
            val millis = if (secondsAndMillis.size > 1) secondsAndMillis[1].toLong() else 0L

            hours * 3600000 + minutes * 60000 + seconds * 1000 + millis
        } catch (e: Exception) {
            Log.e("VttParser", "Error parsing time: $time", e)
            -1L
        }
    }
}
