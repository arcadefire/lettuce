package org.lettux.extension

import org.lettux.core.ActionHandler
import org.lettux.core.State

fun <STATE : State, SLICE : State> ActionHandler<SLICE>.pullback(
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE
): ActionHandler<STATE> {
    return ActionHandler { slice(stateToSlice, sliceToState).handle() }
}

fun <STATE : State> combine(vararg handlers: ActionHandler<STATE>): ActionHandler<STATE> {
    return ActionHandler {
        val actionContext = this
        handlers.forEach { handler -> with(handler) { actionContext.handle() } }
    }
}
