package org.lettux.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import org.lettux.core.Action
import org.lettux.core.Middleware
import org.lettux.core.State
import org.lettux.core.Store
import org.lettux.core.StoreFactory
import org.lettux.core.Subscription

abstract class StoreViewModel<STATE : State> constructor(
    private val storeFactory: StoreFactory<STATE>,
) : Store<STATE>, ViewModel() {

    private val store = storeFactory.get(viewModelScope)

    override val states = store.states

    override fun send(action: Action) = store.send(action)

    override fun <SLICE : State> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>,
        subscription: Subscription<SLICE>?,
        sliceScope: CoroutineScope,
    ): Store<SLICE> {
        return store.slice(
            stateToSlice = stateToSlice,
            sliceToState = sliceToState,
            middlewares = middlewares,
            subscription = subscription,
            sliceScope = sliceScope,
        )
    }
}
