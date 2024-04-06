package org.lettux.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.Store
import org.lettux.createStore
import java.util.concurrent.Flow

abstract class StoreViewModel<STATE : Any> constructor(
    private val store: Store<STATE>,
    private val subscription: Subscription<STATE>? = null,
) : Store<STATE> by store, ViewModel() {

    init {
        subscription?.let {
            subscription.subscribe(states)
                .onEach { send(it) }
                .launchIn(viewModelScope)
        }
    }
}