package org.lettux

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lettux.core.*
import org.lettux.slice.SlicedStatesFlow

fun <STATE : Any> createStore(
    initialState: STATE,
    actionHandler: ActionHandler<STATE>,
    middlewares: List<Middleware> = emptyList(),
    storeScope: CoroutineScope,
): Store<STATE> = DefaultStore(
    states = MutableStateFlow(initialState),
    actionHandler = actionHandler,
    storeScope = storeScope,
    middlewares = middlewares,
)

internal class DefaultStore<STATE>(
    override val states: MutableStateFlow<STATE>,
    private val actionHandler: ActionHandler<STATE>,
    private val storeScope: CoroutineScope,
    middlewares: List<Middleware> = emptyList(),
) : Store<STATE> {

    private class RecorderActionContext<STATE>(
        private val actionContext: ActionContext<STATE>,
    ) : ActionContext<STATE> by actionContext {

        var modifiedState: STATE? = null

        override fun commit(state: STATE): STATE {
            modifiedState = state
            return actionContext.commit(state)
        }
    }

    private val middlewareChain: Chain = middlewares.fold(Chain { actionContext ->
        val previousState = state
        val recorder = RecorderActionContext(actionContext as ActionContext<STATE>)

        with(recorder) {
            with(actionHandler) {
                handle()
            }
        }

        val newState = recorder.modifiedState ?: state
        if (previousState != newState) {
            setState(newState)
            Outcome.StateMutated(newState as Any)
        } else {
            Outcome.NoMutation
        }
    }) { chain, middleware ->
        Chain { actionContext -> middleware.intercept(actionContext, chain) }
    }

    @Synchronized
    private fun setState(newState: STATE) {
        states.value = newState
    }

    override fun send(action: Action): Job {
        val actionContext = DefaultActionContext(
            action = action,
            getState = { states.value },
            setState = ::setState,
            sendToStore = ::send,
        )
        return storeScope.launch(start = CoroutineStart.UNDISPATCHED) {
            middlewareChain.proceed(actionContext as ActionContext<Any>)
        }
    }

    override fun <SLICE : Any> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>,
    ): Store<SLICE> {
        return DefaultStore(
            states = SlicedStatesFlow(states, stateToSlice, sliceToState),
            actionHandler = {
                val actionContext = DefaultActionContext(
                    action = action,
                    getState = { states.value },
                    setState = ::setState,
                    sendToStore = ::send,
                )
                middlewareChain.proceed(actionContext as ActionContext<Any>)
            },
            storeScope = storeScope,
            middlewares = middlewares,
        )
    }
}