package org.lettux

import org.lettux.core.Action
import org.lettux.core.ActionContext

class DefaultActionContext<STATE>(
    override val action: Action,
    private val sendToStore: (ActionContext<Any>) -> Unit,
    private val getState: () -> STATE,
    private val setState: (STATE) -> Unit,
) : ActionContext<STATE> {

    override val state get() = getState()

    override fun send(action: Action): STATE = state.also {
        val innerActionContext = DefaultActionContext(
            action = action,
            sendToStore = sendToStore,
            getState = getState,
            setState = setState,
        ) as ActionContext<Any>
        sendToStore(innerActionContext)
    }

    override fun commit(state: STATE): STATE = state.also { setState(it) }

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
