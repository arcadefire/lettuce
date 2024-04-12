package org.lettux.slice

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import org.lettux.DefaultActionContext
import org.lettux.core.Action
import org.lettux.core.ActionContext
import org.lettux.core.ActionHandler
import org.lettux.core.Chain
import org.lettux.core.Middleware
import org.lettux.core.Outcome

internal class SlicedStatesFlow<SLICE, STATE>(
    private val parent: MutableStateFlow<STATE>,
    private val stateToSlice: (STATE) -> SLICE,
    private val sliceToState: (STATE, SLICE) -> STATE,
) : MutableStateFlow<SLICE> by MutableStateFlow(stateToSlice(parent.value)) {

    override val replayCache: List<SLICE>
        get() = parent.replayCache.map(stateToSlice)

    override var value: SLICE
        get() = stateToSlice(parent.value)
        set(value) {
            parent.value = sliceToState(parent.value, value)
        }

    override fun tryEmit(value: SLICE): Boolean = parent.tryEmit(
        sliceToState(parent.value, value)
    )

    override suspend fun emit(value: SLICE) {
        parent.emit(sliceToState(parent.value, value))
    }

    override suspend fun collect(collector: FlowCollector<SLICE>): Nothing {
        parent.collect { value -> collector.emit(stateToSlice(value)) }
    }
}