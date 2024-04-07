package org.lettux

import org.lettux.core.ActionHandler

fun <STATE: Any, SLICE: Any> ActionHandler<SLICE>.pullback(
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE
) : ActionHandler<STATE> {
    return ActionHandler {
        slice(stateToSlice, sliceToState).handle()
    }
}