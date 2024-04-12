package org.lettux.factory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.lettux.DefaultActionContext
import org.lettux.DefaultStore
import org.lettux.core.ActionContext
import org.lettux.core.ActionHandler
import org.lettux.core.Chain
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.Store
import org.lettux.core.StoreFactory
import org.lettux.extension.defaultLaunch

fun <STATE : Any> storeFactory(
    initialState: STATE,
    actionHandler: ActionHandler<STATE>,
    middlewares: List<Middleware> = emptyList(),
) = MemoizedStoreFactory(
    defaultStoreFactory(
        initialState = initialState,
        actionHandler = actionHandler,
        middlewares = middlewares,
    )
)

fun <STATE : Any> defaultStoreFactory(
    initialState: STATE,
    actionHandler: ActionHandler<STATE>,
    middlewares: List<Middleware> = emptyList(),
): StoreFactory<STATE> = StoreFactory { storeScope ->
    val statesFlow = MutableStateFlow(initialState)

    val pipeline = middlewares.fold(
        Chain { actionContext ->
            actionContext as ActionContext<STATE>

            val oldState = statesFlow.value

            with(actionHandler) {
                with(actionContext) {
                    handle()
                }
            }

            val newState = statesFlow.value
            if (oldState != newState) {
                Outcome.StateMutated(newState as Any)
            } else {
                Outcome.NoMutation
            }
        }
    ) { chain, middleware ->
        Chain { actionContext -> middleware.intercept(actionContext, chain) }
    }

    DefaultStore(
        states = statesFlow,
        dispatch = { action ->
            val actionContext = DefaultActionContext(
                action = action,
                sendToStore = { innerActionContext ->
                    storeScope.defaultLaunch {
                        pipeline.proceed(innerActionContext)
                    }
                },
                getState = { statesFlow.value },
                setState = { statesFlow.value = it },
            )
            pipeline.proceed(actionContext as ActionContext<Any>)
        },
        storeScope = storeScope,
    )
}

class MemoizedStoreFactory<STATE : Any>(
    private val defaultFactory: StoreFactory<STATE>,
) : StoreFactory<STATE> {

    internal var memoizedStore: Store<STATE>? = null

    override fun get(storeScope: CoroutineScope): Store<STATE> {
        if (memoizedStore == null) {
            memoizedStore = defaultFactory.get(storeScope)
        }
        return memoizedStore!!
    }
}
