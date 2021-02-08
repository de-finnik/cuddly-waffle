package de.finnik.music.songs

import java.util.function.Consumer
import java.util.function.UnaryOperator

class SongList: ObservableList<Song>()

open class ObservableList<T>:ArrayList<T>() {
    private val listeners: MutableList<Consumer<ObservableList<T>>> = ArrayList()


    override fun add(element: T): Boolean {
        change()
        return super.add(element)
    }

    override fun add(index: Int, element: T) {
        change()
        super.add(index, element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        change()
        return super.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        change()
        return super.addAll(index, elements)
    }

    override fun clear() {
        change()
        super.clear()
    }

    override fun remove(element: T): Boolean {
        change()
        return super.remove(element)
    }

    private fun change() {
        listeners.forEach { it.accept(this) }
    }

    fun addListener(listener: Consumer<ObservableList<T>>) {
        listeners.add(listener)
        listener.accept(this)
    }
}