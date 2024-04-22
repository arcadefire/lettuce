package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Test

internal class DefaultActionContextTest {

    @Test
    fun `sliced action context should update the parent state as expected`() {
        var parentState = NestedState(PlainState())
        val actionContext = DefaultActionContext(
            action = HandledAction,
            getState = { parentState },
            setState = { parentState = it },
            sendToStore = { Job() },
        )

        val slicedActionContext = actionContext.slice(
            stateToSlice = { it.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        )

        slicedActionContext.commit(PlainState(value = 42))

        parentState shouldBe NestedState(PlainState(value = 42))
    }
}
