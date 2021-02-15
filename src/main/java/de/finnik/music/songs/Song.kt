package de.finnik.music.songs

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import com.bawaviki.youtubedl_android.utils.YoutubeDLUtils
import com.google.gson.Gson
import java.io.*

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
        val info = SongInfo.load(id, dir)
        val title = info?.title
        this.title = title
            ?: try {
                retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            } catch (e: Exception) {
                videoInfo.title
            }
        val artist = info?.artist
        this.artist = artist
            ?: try {
                retrieveMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            } catch (e: Exception) {
                videoInfo.uploader
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

    class SongInfo(val title: String, val artist: String) {
        companion object {
            private val gson = Gson()
            fun load(id: String, dir: File): SongInfo? {
                val file = File(dir, "$id.song")
                if (file.exists().not()) {
                    return null
                }
                val br = BufferedReader(FileReader(file))
                val info = gson.fromJson<SongInfo>(br, SongInfo::class.java)
                return info
            }

            fun store(id: String, dir: File, info: SongInfo) {
                val file = File(dir, "$id.song")
                file.createNewFile()
                val bw = BufferedWriter(FileWriter(file, false))
                bw.write(gson.toJson(info))
                bw.close()
            }
        }
    }
}