package org.lettux.factory

import org.lettux.core.Middleware
import org.lettux.core.StoreFactory

fun <STATE : Any, SLICE : Any> sliceStoreFactory(
    storeFactory: StoreFactory<STATE>,
    stateToSlice: (STATE) -> SLICE,
    sliceToState: (STATE, SLICE) -> STATE,
    middlewares: List<Middleware> = emptyList(),
): StoreFactory<SLICE> = StoreFactory { sliceScope ->
    storeFactory as MemoizedStoreFactory<STATE>
    val parentStore = storeFactory.memoizedStore ?: storeFactory.get(sliceScope)
    parentStore.slice(
        sliceToState = sliceToState,
        stateToSlice = stateToSlice,
        middlewares = middlewares,
        sliceScope = sliceScope,
    )
}
