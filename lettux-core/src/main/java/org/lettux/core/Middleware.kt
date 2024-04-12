package org.lettux.core

fun interface Middleware {
    suspend fun intercept(action: ActionContext<State>, chain: Chain): Outcome
}

sealed class Outcome {
    data class StateMutated(val state: State) : Outcome()
    data object NoMutation : Outcome()
}

fun interface Chain {
    suspend fun proceed(actionContext: ActionContext<State>): Outcome
}
