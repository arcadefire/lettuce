package org.lettux.android

import kotlinx.coroutines.flow.Flow
import org.lettux.core.Action

fun interface Subscription<STATE : Any> {
    fun subscribe(state: Flow<STATE>): Flow<Action>
}
