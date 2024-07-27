package org.lettuce.factory

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.lettuce.core.Middleware
import org.lettuce.core.SliceableStore
import org.lettuce.core.State
import org.lettuce.core.Store
import org.lettuce.core.Subscription

fun <STATE : State, SLICE : State> sliceStore(
    store: Store<STATE>,
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE,
    middlewares: List<Middleware> = emptyList(),
    subscription: Subscription<SLICE>? = null,
): Store<SLICE> {
    return (store as SliceableStore<STATE>).slice(
        sliceToState = sliceToState,
        stateToSlice = stateToSlice,
        middlewares = middlewares,
        sliceScope = store.storeScope,
    ).also { store ->
        subscription?.apply {
            subscribe(store.states)
                .onEach(store::send)
                .launchIn(store.storeScope)
        }
    }
}
