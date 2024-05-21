package org.lettux.factory

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.lettux.core.Middleware
import org.lettux.core.SliceableStore
import org.lettux.core.State
import org.lettux.core.Store
import org.lettux.core.StoreFactory
import org.lettux.core.Subscription

fun <STATE : State, SLICE : State> sliceStore(
    storeFactory: StoreFactory<STATE>,
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE,
    middlewares: List<Middleware> = emptyList(),
    subscription: Subscription<SLICE>? = null,
): StoreFactory<SLICE> = StoreFactory { sliceScope ->
    storeFactory as MemoizedStoreFactory<STATE>
    val parentStore =
        (storeFactory.memoizedStore ?: storeFactory.get(sliceScope)) as SliceableStore<STATE>
    parentStore.slice(
        sliceToState = sliceToState,
        stateToSlice = stateToSlice,
        middlewares = middlewares,
        sliceScope = sliceScope,
    ).also { store ->
        subscription?.apply {
            subscribe(store.states)
                .onEach(store::send)
                .launchIn(sliceScope)
        }
    }
}

fun <STATE : State, SLICE : State> sliceStore(
    store: Store<STATE>,
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE,
    middlewares: List<Middleware> = emptyList(),
    subscription: Subscription<SLICE>? = null,
): StoreFactory<SLICE> = StoreFactory { sliceScope ->
    (store as SliceableStore<STATE>).slice(
        sliceToState = sliceToState,
        stateToSlice = stateToSlice,
        middlewares = middlewares,
        sliceScope = sliceScope,
    ).also { store ->
        subscription?.apply {
            subscribe(store.states)
                .onEach(store::send)
                .launchIn(sliceScope)
        }
    }
}
