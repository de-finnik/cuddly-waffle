package de.finnik.music.download

import android.util.Log
import com.bawaviki.youtubedl_android.DownloadProgressCallback
import com.bawaviki.youtubedl_android.YoutubeDL
import com.bawaviki.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.util.concurrent.Executors

class Downloader {
    fun download(id: String, dir: File, callback: DownloadProgressCallback) {
        val request = YoutubeDLRequest(id)
        request.setOption("-x")
        request.setOption("--write-info-json")
        request.setOption("--write-thumbnail")
        request.setOption("--cache-dir", File(dir.parentFile, ".cache").absolutePath)
        request.setOption("--add-metadata")
        request.setOption("-o", dir.absolutePath + "/%(id)s.%(ext)s")
        YoutubeDL.getInstance().execute(request, callback)
    }
}

class DownloadTask {
    fun download(id: String, dir: File, callback: DownloadProgressCallback) {
        Executors.newSingleThreadExecutor().execute {
            Downloader().download(id, dir, callback)
        }
    }
}