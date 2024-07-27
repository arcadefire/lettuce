package io.github.arcadefire.lettuce.core

fun interface ActionHandler<S : State> {
    suspend fun ActionContext<S>.handle(action: Action)
}
