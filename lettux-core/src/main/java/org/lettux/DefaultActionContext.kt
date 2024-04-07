package org.lettux

import org.lettux.core.Action
import org.lettux.core.ActionContext

class DefaultActionContext<STATE>(
    override val action: Action,
    private val getState: () -> STATE,
    private val setState: (STATE) -> Unit,
    private val send: (Action) -> Unit,
) : ActionContext<STATE> {

    override val state get() = getState()

    override fun send(action: Action): STATE = state.also { this.send(action) }

    override fun commit(state: STATE): STATE = state.also {
        setState(it)
    }

    override fun <SLICE> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (state: STATE, slice: SLICE) -> STATE,
    ): ActionContext<SLICE> {
        return DefaultActionContext(
            action = action,
            getState = { stateToSlice(getState()) },
            setState = { slicedState -> setState(sliceToState(state, slicedState)) },
            send = send,
        )
    }
}