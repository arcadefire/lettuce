package org.lettux

import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test
import org.lettux.core.Action

internal class DefaultActionContextTest {

    private data object TestAction : Action

    private data class InnerState(val value: Int = 0)
    private data class ParentState(val innerState: InnerState = InnerState())

    @Test
    fun `sliced action context should update the parent state as expected`() {
        var parentState = ParentState(InnerState())
        val actionContext = DefaultActionContext(
            action = TestAction,
            getState = { parentState },
            setState = { parentState = it },
            sendToStore = {},
        )

        val slicedActionContext = actionContext.slice(
            stateToSlice = { it.innerState},
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        )

        slicedActionContext.commit(InnerState(value = 42))

        parentState shouldBe ParentState(InnerState(value = 42))
    }
}