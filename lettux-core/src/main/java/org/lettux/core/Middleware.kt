package org.lettux.core

fun interface Middleware {
    suspend fun intercept(action: ActionContext<Any>, chain: Chain): Outcome
}

sealed class Outcome {
    data class StateMutated(val state: Any) : Outcome()
    data object NoMutation : Outcome()
}

fun interface Chain {
    suspend fun proceed(actionContext: ActionContext<Any>): Outcome
}
