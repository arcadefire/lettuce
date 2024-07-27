package org.lettuce.android

import kotlinx.coroutines.CoroutineScope
import org.lettuce.core.ActionHandler
import org.lettuce.core.Middleware
import org.lettuce.core.State
import org.lettuce.core.Store
import org.lettuce.core.Subscription
import org.lettuce.factory.createStore

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