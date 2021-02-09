package de.finnik.music.songs

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import com.bawaviki.youtubedl_android.utils.YoutubeDLUtils
import java.io.File

class Song(val id: String, private val dir: File) {
    val title: String
    val artist: String

    val videoInfo: VideoInfo = YoutubeDLUtils.readInfoFromJson(File(dir, "$id.info.json"))
    val thumbnail = if (File(dir, "$id.webp").exists()) BitmapFactory.decodeFile(
        File(
            dir,
            "$id.webp"
        ).absolutePath
    ) else BitmapFactory.decodeFile(File(dir, "$id.jpg").absolutePath)
    val audio: File
        get() {
            return AUDIO_FORMATS.map { File(dir, "$id.$it") }.filter { it.exists() }.first()
        }

    init {
        title =
            try {
                retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            } catch (e: Exception) {
                videoInfo.uploader
            }
        artist =
            try {
                retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            } catch (e: Exception) {
                videoInfo.title
            }
    }

    private fun retrieveMetadata(keyCode: Int): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audio.absolutePath)
        return retriever.extractMetadata(keyCode)
    }

    override fun toString(): String {
        return "$title - $artist"
    }

    companion object {
        fun findSongs(dir: File): List<Song> {
            return dir.listFiles()
                .filter { AUDIO_FORMATS.contains(it.extension) }
                .filter { File(dir, "${it.nameWithoutExtension}.info.json").exists() }
                .filter {
                    File(dir, "${it.nameWithoutExtension}.webp").exists() || File(
                        dir,
                        "${it.nameWithoutExtension}.jpg"
                    ).exists()
                }
                .map { Song(it.nameWithoutExtension, dir) }

        }

        val AUDIO_FORMATS = arrayOf("aac", "flac", "mp3", "m4a", "opus", "vorbis", "wav")
    }
}