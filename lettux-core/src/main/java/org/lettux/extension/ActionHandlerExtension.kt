package org.lettux.extension

import org.lettux.core.ActionHandler

fun <STATE : Any, SLICE : Any> ActionHandler<SLICE>.pullback(
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE
): ActionHandler<STATE> {
    return ActionHandler { slice(stateToSlice, sliceToState).handle() }
}

fun <STATE : Any> combine(vararg handlers: ActionHandler<STATE>): ActionHandler<STATE> {
    return ActionHandler {
        val actionContext = this
        handlers.forEach { handler -> with(handler) { actionContext.handle() } }
    }
}
