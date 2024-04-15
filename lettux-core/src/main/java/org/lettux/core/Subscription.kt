package org.lettux.core

import kotlinx.coroutines.flow.Flow

fun interface Subscription<STATE : State> {
    fun subscribe(states: Flow<STATE>): Flow<Action>
}
