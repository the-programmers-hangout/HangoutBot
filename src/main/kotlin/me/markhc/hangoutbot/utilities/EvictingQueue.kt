package me.markhc.hangoutbot.utilities

import java.util.*

class EvictingQueue<E> private constructor(val maxSize: Int) : MutableCollection<E> {
    companion object {
        fun <E> create(maxSize: Int): EvictingQueue<E> {
            require(maxSize >= 0) { "maxSize ($maxSize) must >= 0" }
            return EvictingQueue(maxSize)
        }
    }

    private val queue: Queue<E> = ArrayDeque(maxSize)

    override val size: Int
        get() = queue.size

    override fun add(element: E): Boolean {
        if (maxSize == 0)
            return true

        if (size == maxSize)
            queue.remove()

        queue.add(element)
        return true
    }

    override fun addAll(elements: Collection<E>) = queue.addAll(elements)
    override fun clear() = queue.clear()
    override fun isEmpty() = size == 0
    override fun remove(element: E) = queue.remove(element)
    override fun containsAll(elements: Collection<E>) = queue.containsAll(elements)
    override fun removeAll(elements: Collection<E>) = queue.removeAll(elements)
    override fun retainAll(elements: Collection<E>) = queue.retainAll(elements)
    override fun iterator() = queue.iterator()
    override operator fun contains(element: E) = queue.contains(element)
}