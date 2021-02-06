package de.finnik.music

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import com.bawaviki.youtubedl_android.mapper.VideoThumbnail
import com.bawaviki.youtubedl_android.utils.YoutubeDLUtils
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException

class Song(val id: String, dir: File) {
    var title: String get() {
        try {
            return retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        } catch (e: Exception) {
            return videoInfo.uploader
        }
    }
    var artist: String get() {
        try {
            return retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        } catch (e: Exception) {
            return videoInfo.title
        }
    }
    val videoInfo: VideoInfo = YoutubeDLUtils.readInfoFromJson(File(dir, "$id.info.json"))
    val thumbnail = if(File(dir, "$id.webp").exists()) BitmapFactory.decodeFile(File(dir, "$id.webp").absolutePath) else BitmapFactory.decodeFile(File(dir, "$id.jpg").absolutePath)
    val audio = File(dir, "$id.opus")
    init {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audio.absolutePath)
        title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        Log.i("TAG", "$title $artist: ")
    }

    private fun retrieveMetadata(keyCode: Int): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audio.absolutePath)
        return retriever.extractMetadata(keyCode)
    }

    companion object {
        fun findSongs(dir: File): List<Song> {
            return dir.listFiles()
                .filter { it.extension == "opus" }
                .filter { File(dir, "${it.nameWithoutExtension}.info.json").exists() }
                .filter { File(dir, "${it.nameWithoutExtension}.webp").exists() || File(dir, "${it.nameWithoutExtension}.jpg").exists()}
                .map { Song(it.nameWithoutExtension, dir) }

        }
    }
}