package org.lettuce.core

fun interface ActionHandler<S : State> {
    suspend fun ActionContext<S>.handle(action: Action)
}
