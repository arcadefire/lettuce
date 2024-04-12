package org.lettux.core

import kotlinx.coroutines.CoroutineScope

fun interface StoreFactory<STATE : Any> {
    fun get(storeScope: CoroutineScope): Store<STATE>
}