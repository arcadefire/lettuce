package org.lettux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.lettux.core.Action
import org.lettux.core.Chain
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.SliceableStore
import org.lettux.core.State
import org.lettux.core.Store
import org.lettux.extension.defaultLaunch
import org.lettux.extension.state
import org.lettux.slice.SlicedStatesFlow

internal class DefaultStore<STATE : State>(
    override val states: MutableStateFlow<STATE>,
    override val storeScope: CoroutineScope,
    private val doSend: suspend (Action) -> Outcome,
) : Store<STATE>, SliceableStore<STATE> {

    override fun send(action: Action): Job = storeScope.defaultLaunch { doSend(action) }

    override fun <SLICE : State> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>,
        sliceScope: CoroutineScope,
    ): Store<SLICE> {
        return DefaultStore(
            states = SlicedStatesFlow(states, stateToSlice, sliceToState),
            storeScope = sliceScope,
            doSend = { action ->
                val bridgeChain = Chain { sliceAction ->
                    val parentOutcome = doSend(sliceAction)
                    if (parentOutcome is Outcome.StateMutated) {
                        Outcome.StateMutated(stateToSlice(parentOutcome.state as STATE))
                    } else {
                        Outcome.NoMutation
                    }
                }
                val chain = middlewares.reversed().fold(bridgeChain) { chain, middleware ->
                    Chain { action -> middleware.intercept(action, stateToSlice(state), chain) }
                }
                chain.proceed(action)
            },
        )
    }
}
