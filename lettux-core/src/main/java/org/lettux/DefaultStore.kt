package org.lettux

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lettux.core.*
import org.lettux.slice.SlicedStatesFlow

fun interface StoreFactory<STATE : Any> {
    fun create(
        storeScope: CoroutineScope,
    ): Store<STATE>
}

fun <STATE : Any> storeFactory(
    initialState: STATE,
    actionHandler: ActionHandler<STATE>,
    middlewares: List<Middleware> = emptyList(),
): StoreFactory<STATE> = StoreFactory { storeScope ->
    val statesFlow = MutableStateFlow(initialState)
    val pipeline = middlewares.fold(Chain { actionContext ->
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
    }) { chain, middleware ->
        Chain { actionContext -> middleware.intercept(actionContext, chain) }
    }

    DefaultStore(
        states = statesFlow,
        dispatch = { action ->
            val actionContext = DefaultActionContext(
                action = action,
                getState = { statesFlow.value },
                setState = { statesFlow.value = it },
                sendToStore = {},
            )
            pipeline.proceed(actionContext as ActionContext<Any>)
        },
        storeScope = storeScope,
    )
}

internal class DefaultStore<STATE>(
    override val states: MutableStateFlow<STATE>,
    private val dispatch: suspend (Action) -> Outcome,
    private val storeScope: CoroutineScope,
) : Store<STATE> {

    override fun send(action: Action): Job {
        return storeScope.launch(start = CoroutineStart.UNDISPATCHED) {
            dispatch(action)
        }
    }

    override fun <SLICE : Any> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>,
    ): Store<SLICE> {
        return DefaultStore(
            states = SlicedStatesFlow(states, stateToSlice, sliceToState),
            dispatch = { action ->
                val bridgeChain = Chain {
                    val parentOutcome = dispatch(action)
                    if (parentOutcome is Outcome.StateMutated) {
                        Outcome.StateMutated(stateToSlice(parentOutcome.state as STATE))
                    } else {
                        Outcome.NoMutation
                    }
                }
                val actionContext = DefaultActionContext(
                    action = action,
                    getState = { states.value },
                    setState = { states.value = it },
                    sendToStore = ::send,
                )
                middlewares.fold(bridgeChain) { chain, middleware ->
                    Chain { actionContext -> middleware.intercept(actionContext, chain) }
                }.proceed(actionContext as ActionContext<Any>)
            },
            storeScope = storeScope,
        )
    }
}