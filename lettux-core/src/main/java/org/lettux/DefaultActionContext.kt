package org.lettux

import org.lettux.core.Action
import org.lettux.core.ActionContext
import org.lettux.core.State

class DefaultActionContext<STATE>(
    override val action: Action,
    private val sendToStore: (ActionContext<State>) -> Unit,
    private val getState: () -> STATE,
    private val setState: (STATE) -> Unit,
) : ActionContext<STATE> {

    override val state get() = getState()

    override fun send(action: Action) {
        val innerActionContext = DefaultActionContext(
            action = action,
            sendToStore = sendToStore,
            getState = getState,
            setState = setState,
        ) as ActionContext<State>
        sendToStore(innerActionContext)
    }

    override fun commit(state: STATE) { setState(state) }

    override fun <SLICE> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (state: STATE, slice: SLICE) -> STATE,
    ): ActionContext<SLICE> {
        return DefaultActionContext(
            action = action,
            getState = { stateToSlice(getState()) },
            setState = { slicedState -> setState(sliceToState(state, slicedState)) },
            sendToStore = sendToStore,
        )
    }
}
