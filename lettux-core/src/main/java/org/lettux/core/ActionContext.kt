package org.lettux.core

interface ActionContext<STATE> {
    val state: STATE
    val action: Action
    fun send(action: Action): STATE
    fun commit(state: STATE): STATE
    fun <SLICE> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
    ) : ActionContext<SLICE>
}