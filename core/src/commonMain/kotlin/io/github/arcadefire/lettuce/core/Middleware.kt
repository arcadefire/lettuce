package io.github.arcadefire.lettuce.core

fun interface Middleware {
    suspend fun intercept(action: Action, state: State, chain: Chain): Outcome
}

sealed class Outcome {
    data class StateMutated(val state: State) : Outcome()
    data object NoMutation : Outcome()
}

fun interface Chain {
    suspend fun proceed(action: Action): Outcome
}
