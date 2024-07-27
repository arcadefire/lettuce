package org.lettux.extension

import org.lettux.core.ActionHandler
import org.lettux.core.State
import org.lettux.core.Subscription

fun <STATE : State, SLICE : State> ActionHandler<SLICE>.pullback(
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE
): ActionHandler<STATE> {
    return ActionHandler { action ->
        val actionContext = slice(stateToSlice, sliceToState)
        with(actionContext) { handle(action) }
    }
}

fun <STATE : State> combine(vararg handlers: ActionHandler<STATE>): ActionHandler<STATE> {
    return ActionHandler {
        handlers.forEach { handler -> with(handler) { handle(it) } }
    }
}

operator fun <S : State> ActionHandler<S>.plus(another: ActionHandler<S>): ActionHandler<S> {
    return combine(this, another)
}
