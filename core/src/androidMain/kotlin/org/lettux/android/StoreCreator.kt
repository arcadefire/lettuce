package org.lettux.android

import kotlinx.coroutines.CoroutineScope
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.State
import org.lettux.core.Store
import org.lettux.core.Subscription
import org.lettux.factory.createStore

fun interface StoreCreator<S : State> {
    fun create(scope: CoroutineScope): Store<S>
}

fun <S : State> storeCreator(
    initialState: S,
    actionHandler: ActionHandler<S>,
    middlewares: List<Middleware> = emptyList(),
    subscription: Subscription<S>? = null,
): StoreCreator<S> = StoreCreator { scope ->
    createStore(
        initialState = initialState,
        actionHandler = actionHandler,
        middlewares = middlewares,
        subscription = subscription,
        storeScope = scope
    )
}