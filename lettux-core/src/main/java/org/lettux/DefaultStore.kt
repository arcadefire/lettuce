package org.lettux

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lettux.core.*
import org.lettux.extension.defaultLaunch
import org.lettux.slice.SlicedStatesFlow

internal class DefaultStore<STATE>(
    override val states: MutableStateFlow<STATE>,
    private val dispatch: suspend (Action) -> Outcome,
    private val storeScope: CoroutineScope,
) : Store<STATE> {

    override fun send(action: Action): Job = storeScope.defaultLaunch { dispatch(action) }

    override fun <SLICE : Any> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>,
        sliceScope: CoroutineScope,
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
                val chain = middlewares.fold(bridgeChain) { chain, middleware ->
                    Chain { actionContext -> middleware.intercept(actionContext, chain) }
                }
                val actionContext = DefaultActionContext(
                    action = action,
                    getState = { states.value },
                    setState = { states.value = it },
                    sendToStore = {
                        storeScope.defaultLaunch { chain.proceed(it) }
                    },
                )
                chain.proceed(actionContext as ActionContext<Any>)
            },
            storeScope = sliceScope,
        )
    }
}