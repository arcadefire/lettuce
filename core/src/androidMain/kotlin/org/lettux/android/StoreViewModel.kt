package org.lettux.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.lettux.core.Action
import org.lettux.core.State
import org.lettux.core.Store

abstract class StoreViewModel<S : State>(storeCreator: StoreCreator<S>) : Store<S>, ViewModel() {

    private val store: Store<S> by lazy {
        storeCreator.create(this.viewModelScope)
    }

    override val storeScope: CoroutineScope
        get() = viewModelScope

    override val states: StateFlow<S>
        get() = store.states

    override fun send(action: Action) = store.send(action)
}