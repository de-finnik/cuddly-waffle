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
        request.setOption("--audio-format", "opus")
        request.setOption("--write-info-json")
        request.setOption("--write-thumbnail")
        request.setOption("--cache-dir", File(dir.parentFile, ".cache").absolutePath)
        request.setOption("--add-metadata")
        request.setOption("-o", dir.absolutePath +"/%(id)s.%(ext)s")
        val response = YoutubeDL.getInstance().execute(request)
        Log.i("TAG", "download: $response")
    }
}