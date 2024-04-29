package org.lettux.core

fun interface ActionHandler<S : State> {
    context(ActionHandlerContext<S>)
    suspend fun handle(action: Action)
}
