package de.finnik.music

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.bawaviki.ffmpeg.FFmpeg
import com.bawaviki.youtubedl_android.YoutubeDL
import com.bawaviki.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.util.concurrent.Executors

class Downloader {
    fun download(id: String, dir: File) {
        val request = YoutubeDLRequest(id)
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

class DownloadTask {
    fun download(id: String, dir: File) {
        Executors.newSingleThreadExecutor().execute {
            Downloader().download(id, dir)
        }
    }
}