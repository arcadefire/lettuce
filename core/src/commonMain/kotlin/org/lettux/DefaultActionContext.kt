package org.lettux

import kotlinx.coroutines.Job
import org.lettux.core.Action
import org.lettux.core.ActionContext
import org.lettux.core.State

class DefaultActionContext<STATE : State>(
    private val sendFunction: (Action) -> Job,
    private val getState: () -> STATE,
    private val setState: (STATE) -> Unit,
) : ActionContext<STATE> {

    override val state get() = getState()

    override fun commit(state: STATE) { setState(state) }

    override fun send(action: Action) : Job = sendFunction(action)

    override fun <SLICE : State> slice(
        stateToSlice: (STATE) -> SLICE,
        sliceToState: (STATE, SLICE) -> STATE
    ): ActionContext<SLICE> {
        return DefaultActionContext(
            getState = { stateToSlice(getState()) },
            setState = { slicedState -> setState(sliceToState(state, slicedState)) },
            sendFunction = sendFunction,
        )
    }
}
