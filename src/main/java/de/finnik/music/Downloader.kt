package de.finnik.music

import android.util.Log
import com.bawaviki.ffmpeg.FFmpeg
import com.bawaviki.youtubedl_android.YoutubeDL
import com.bawaviki.youtubedl_android.YoutubeDLRequest
import java.io.File

class Downloader {
    fun download(id: String, dir: File) {
        val url = "https://youtu.be/$id"
        val request = YoutubeDLRequest(url)
        request.setOption("-x")
        request.setOption("--audio-format", "mp3")
        request.setOption("--write-info-json")
        request.setOption("--write-thumbnail")
        request.setOption("-o", dir.absolutePath +"/%(id)s.mp3")
        val response = YoutubeDL.getInstance().execute(request)
        Log.i("TAG", "download: $response")
    }
}