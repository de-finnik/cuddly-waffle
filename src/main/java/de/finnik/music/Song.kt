package de.finnik.music

import android.graphics.BitmapFactory
import android.util.Log
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import com.bawaviki.youtubedl_android.mapper.VideoThumbnail
import com.bawaviki.youtubedl_android.utils.YoutubeDLUtils
import java.io.File

class Song(val id: String, dir: File) {
    val videoInfo: VideoInfo = YoutubeDLUtils.readInfoFromJson(File(dir, "$id.mp3.info.json"))
    val thumbnail = BitmapFactory.decodeFile(File(dir, "$id.mp3.webp").absolutePath)
    val audio = File(dir, "$id.mp3")
    companion object {
        fun findSongs(dir: File): List<Song> {
            return dir.listFiles()
                .filter { it.extension == "mp3" }
                .filter { File(dir, "${it.name}.info.json").exists() }
                .filter { File(dir, "${it.name}.webp").exists() }
                .map { Song(it.nameWithoutExtension, dir) }

        }
    }
}