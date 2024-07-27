package io.github.arcadefire.lettuce

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import io.github.arcadefire.lettuce.core.Action
import io.github.arcadefire.lettuce.core.Chain
import io.github.arcadefire.lettuce.core.Middleware
import io.github.arcadefire.lettuce.core.Outcome
import io.github.arcadefire.lettuce.core.SliceableStore
import io.github.arcadefire.lettuce.core.State
import io.github.arcadefire.lettuce.core.Store
import io.github.arcadefire.lettuce.extension.defaultLaunch
import io.github.arcadefire.lettuce.extension.state
import io.github.arcadefire.lettuce.slice.SlicedStatesFlow

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
