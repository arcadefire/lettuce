package org.lettux.core

interface ActionContext<STATE> {
    val state: STATE
    val action: Action
    fun send(action: Action)
    fun commit(state: STATE)
    fun <SLICE> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
    ): ActionContext<SLICE>
}
