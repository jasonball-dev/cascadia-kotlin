package service

import gui.Refreshable

/**
 * Abstract class, which provides a function call a function to connect all refreshables
 */
abstract class AbstractRefreshingService {

    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Adds a new [Refreshable] to the list of refreshables.
     *
     * @param newRefreshable The [Refreshable] to be added
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Adds each of the provided [Refreshable]s to the list of refreshables.
     *
     * @param method The [Refreshable]s to be added
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) = refreshables.forEach { it.method() }
}