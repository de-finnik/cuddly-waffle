package de.finnik.music.songs

import android.util.Log
import java.util.function.Consumer
import java.util.function.UnaryOperator

class SongList: ObservableList<Song>()

open class ObservableList<T>:ArrayList<T>() {
    private val listeners: MutableList<Consumer<ObservableList<T>>> = ArrayList()


    override fun add(element: T): Boolean {
        val boolean = super.add(element)
        change()
        return boolean
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        change()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val addAll = super.addAll(elements)
        change()
        return addAll
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val addAll = super.addAll(index, elements)
        change()
        return addAll
    }

    override fun clear() {
        super.clear()
        change()
    }

    override fun remove(element: T): Boolean {
        val remove = super.remove(element)
        change()
        return remove
    }

    private fun change() {
        listeners.forEach { it.accept(this) }
    }

    fun addListener(listener: Consumer<ObservableList<T>>) {
        listeners.add(listener)
    }
}