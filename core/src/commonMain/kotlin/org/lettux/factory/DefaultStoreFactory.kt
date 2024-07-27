package org.lettux.factory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.lettux.DefaultActionContext
import org.lettux.DefaultStore
import org.lettux.core.ActionHandler
import org.lettux.core.Chain
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.State
import org.lettux.core.Store
import org.lettux.core.Subscription

fun <STATE : State> createStore(
    initialState: STATE,
    actionHandler: ActionHandler<STATE>,
    middlewares: List<Middleware> = emptyList(),
    subscription: Subscription<STATE>? = null,
    storeScope: CoroutineScope,
): Store<STATE> {
    lateinit var store: Store<STATE>

    val statesFlow = MutableStateFlow(initialState)
    val pipeline = middlewares
        .reversed()
        .fold(
            Chain { action ->
                val actionContext = DefaultActionContext(
                    sendFunction = store::send,
                    getState = { statesFlow.value },
                    setState = { statesFlow.value = it },
                )
                val oldState = statesFlow.value

                with(actionHandler) {
                    with(actionContext) {
                        handle(action)
                    }
                }

                val newState = statesFlow.value
                if (oldState != newState) {
                    Outcome.StateMutated(newState)
                } else {
                    Outcome.NoMutation
                }
            }
        ) { chain, middleware ->
            Chain { action -> middleware.intercept(action, statesFlow.value, chain) }
        }

    return DefaultStore(
        states = statesFlow,
        storeScope = storeScope,
        doSend = { action -> pipeline.proceed(action) },
    ).also {
        store = it
        subscription?.apply {
            subscribe(store.states)
                .onEach(store::send)
                .launchIn(storeScope)
        }
    }
}

