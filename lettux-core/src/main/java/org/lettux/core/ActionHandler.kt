package org.lettux.core

fun interface ActionHandler<STATE> {
    suspend fun ActionContext<STATE>.handle()
}
