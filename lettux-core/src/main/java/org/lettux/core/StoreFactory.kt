package org.lettux.core

import kotlinx.coroutines.CoroutineScope

fun interface StoreFactory<STATE : State> {
    fun get(storeScope: CoroutineScope): Store<STATE>
}
