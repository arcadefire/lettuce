package org.lettux.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.lettux.StoreFactory
import org.lettux.core.Action
import org.lettux.core.Middleware
import org.lettux.core.Store

abstract class StoreViewModel<STATE : Any> constructor(
    private val storeFactory: StoreFactory<STATE>,
    private val subscription: Subscription<STATE>? = null,
) : Store<STATE>, ViewModel() {

    private val store = storeFactory.create(viewModelScope)

    init {
        subscription?.let {
            subscription.subscribe(states)
                .onEach { send(it) }
                .launchIn(viewModelScope)
        }
    }

    override val states = store.states

    override fun send(action: Action) = store.send(action)

    override fun <SLICE : Any> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE,
        middlewares: List<Middleware>
    ): Store<SLICE> {
        return store.slice(
            stateToSlice = stateToSlice,
            sliceToState = sliceToState,
            middlewares = middlewares
        )
    }
}