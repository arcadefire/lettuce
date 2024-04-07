package org.lettux.slice

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

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