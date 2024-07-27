package io.github.arcadefire.lettuce.slice

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

internal class SlicedStatesFlow<SLICE, STATE>(
    private val sourceFlow: MutableStateFlow<STATE>,
    private val stateToSlice: (STATE) -> SLICE,
    private val sliceToState: (STATE, SLICE) -> STATE,
) : MutableStateFlow<SLICE> by MutableStateFlow(stateToSlice(sourceFlow.value)) {

    override val replayCache: List<SLICE>
        get() = sourceFlow.replayCache.map(stateToSlice)

    override var value: SLICE
        get() = stateToSlice(sourceFlow.value)
        set(value) {
            sourceFlow.value = sliceToState(sourceFlow.value, value)
        }

    override fun tryEmit(value: SLICE): Boolean = sourceFlow.tryEmit(
        sliceToState(sourceFlow.value, value)
    )

    override suspend fun emit(value: SLICE) {
        sourceFlow.emit(sliceToState(sourceFlow.value, value))
    }

    override suspend fun collect(collector: FlowCollector<SLICE>): Nothing {
        sourceFlow.collect { value -> collector.emit(stateToSlice(value)) }
    }
}
