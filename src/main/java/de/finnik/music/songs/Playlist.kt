package de.finnik.music.songs

import android.util.Log
import com.google.android.gms.common.util.Strings
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.Buffer

class Playlist(val ids: List<String>, val name: String) {
    operator fun get(index: Int): String {
        return ids[index]
    }

    fun size(): Int {
        return ids.size
    }

    fun contains(id: String): Boolean {
        return ids.contains(id)
    }

    override fun toString(): String {
        var string = "$name:"
        ids.forEach {  string += " '$it'"}
        return string
    }

    companion object {
        fun findPlaylists(dir: File): List<Playlist> {
            return dir.listFiles().map {
                Gson().fromJson(FileReader(it), Playlist::class.java)
            }
        }
    }
}

open class PlaylistPlayer {
    open var playlist: Playlist = Playlist(listOf(), "")
    protected var index = 0
        set(value) {
            var mod = value % playlist.size()
            if (mod < 0) {
                mod += playlist.size()
            }
            field = mod
        }


    fun play(index: Int) {
        this.index = index
    }

    fun next() {
        index++
    }

    fun previous() {
        index--
    }

    fun getCurrent(): String {
        return playlist[index]
    }
}

class SongPlayer(val songs: List<Song>, playlist: Playlist) : PlaylistPlayer() {
    init {
        this.playlist = playlist
    }

    fun getCurrentSong(): Song {
        return songs.filter { it.id == getCurrent() }.first()
    }
}