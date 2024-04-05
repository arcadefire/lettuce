package org.lettux

import org.lettux.core.Action
import org.lettux.core.ActionContext
import org.lettux.core.Store
import org.lettux.core.state

class DefaultActionContext<STATE>(
    override val action: Action,
    private val store: Store<STATE>,
    private val setState: (STATE) -> Unit,
) : ActionContext<STATE> {

    override val state get() = store.state

    override fun send(action: Action): STATE = state.also { store.send(action) }

    override fun commit(state: STATE): STATE = state.also { setState(it) }
}