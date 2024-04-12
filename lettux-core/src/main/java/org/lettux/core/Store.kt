package org.lettux.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface Store<STATE> {
    val states: StateFlow<STATE>

    fun send(action: Action): Job

    fun <SLICE : Any> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware> = emptyList(),
        sliceScope: CoroutineScope,
    ): Store<SLICE>
}

val <STATE> Store<STATE>.state: STATE get() = states.value