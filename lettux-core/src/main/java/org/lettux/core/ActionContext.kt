package org.lettux.core

import kotlinx.coroutines.Job

interface ActionContext<STATE : State> {
    val state: STATE
    val action: Action
    fun send(action: Action) : Job
    fun commit(state: STATE)
    fun <SLICE : State> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
    ): ActionContext<SLICE>
}
