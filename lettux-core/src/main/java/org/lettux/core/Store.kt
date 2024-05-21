package org.lettux.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents an action that can be dispatched to the store.
 */

interface Action

/**
 * Represents an action that can be dispatched to the store.
 */
interface State

/**
 * Represents a store that holds a state and allows dispatching actions.
 *
 * @param STATE The type of the state held by the store.
 */
interface Store<STATE : State> {

    /**
     * A StateFlow representing the current state of the store.
     */
    val states: StateFlow<STATE>

    /**
     * Launches the action on the storeScope and returns a
     * [Job] representing the coroutine in which the action is processed.
     */
    fun send(action: Action): Job
}

internal interface SliceableStore<STATE : State> {
    /**
     * Creates a slice of the store's state.
     */
    fun <SLICE : State> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware> = emptyList(),
        sliceScope: CoroutineScope,
    ): Store<SLICE>
}
