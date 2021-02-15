package de.finnik.music.songs

import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.function.Consumer

class Playlist(val ids: MutableList<String>, val name: String) {
    operator fun get(index: Int): String {
        return ids[index]
    }

    fun add(id: String) {
        if (ids.contains(id).not()) {
            ids.add(id)
        }
    }

    fun remove(id: String) {
        if (ids.contains(id)) {
            ids.remove(id)
        }
    }

    fun createQuery(index: Int):List<String> {
        val query = ids.toList()
        Collections.rotate(query, size() - index)
        return query
    }

    fun size(): Int {
        return ids.size
    }

    fun contains(id: String): Boolean {
        return ids.contains(id)
    }

    override fun toString(): String {
        var string = "$name:"
        ids.forEach { string += " '$it'" }
        return string
    }

    companion object {
        fun findPlaylists(dir: File): List<Playlist> {
            return dir.listFiles().map {
                Gson().fromJson(FileReader(it), Playlist::class.java)
            }
        }

        fun storePlaylist(playlist: Playlist, dir: File) {
            val writer = BufferedWriter(FileWriter(File(dir, playlist.name)))
            writer.write(Gson().toJson(playlist))
            writer.close()
        }
    }
}


class PlaylistStore(val dir: File) {
    lateinit var playlists: List<Playlist>

    private val editListeners = mutableListOf<Consumer<Playlist>>()
    private val changeListeners = mutableListOf<Consumer<PlaylistStore>>()

    init {
        load()
    }

    fun add(song: Song, playlistName: String) {
        val playlist = playlists.filter { it.name == playlistName }.first()
        playlist.add(song.id)
        store()
        callEdit(playlist)
    }

    fun remove(song: Song, playlistName: String) {
        val playlist = playlists.filter { it.name == playlistName }.first()
        playlist.remove(song.id)
        store()
        callEdit(playlist)
    }

    fun store() {
        playlists.forEach { Playlist.storePlaylist(it, dir) }
    }

    fun addEditListener(consumer: Consumer<Playlist>){
        editListeners.add(consumer)
    }

    fun addChangeListener(consumer: Consumer<PlaylistStore>) {
        changeListeners.add(consumer)
    }

    private fun callEdit(playlist: Playlist) {
        editListeners.forEach { it.accept(playlist) }
    }

    private fun callChange() {
        changeListeners.forEach { it.accept(this) }
    }

    fun load() {
        playlists = Playlist.findPlaylists(dir)
    }

    fun newPlaylist(string: String): Boolean {
        if (playlists.any { it.name == string }) {
            return false
        }
        playlists = playlists.plus(Playlist(mutableListOf(), string))
        store()
        callChange()
        return true
    }
}