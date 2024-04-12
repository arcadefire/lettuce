package org.lettux.core

fun interface ActionHandler<STATE : State> {
    suspend fun ActionContext<STATE>.handle()
}
