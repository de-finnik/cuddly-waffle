package de.finnik.music.songs

open class QueryPlayer {
    var queryList: List<String> = listOf()
    protected var index = 0
        set(value) {
            var mod = value % queryList.size
            if (mod < 0) {
                mod += queryList.size
            }
            field = mod
        }

    fun play(list: List<String>) {
        queryList = list
        this.index = 0
    }

    fun next() {
        index++
    }

    fun previous() {
        index--
    }

    fun getCurrent(): String {
        return queryList[index]
    }
}

class SongPlayer(val songs: List<Song>) : QueryPlayer() {
    fun getCurrentSong(): Song {
        return songs.filter { it.id == getCurrent() }.first()
    }
}