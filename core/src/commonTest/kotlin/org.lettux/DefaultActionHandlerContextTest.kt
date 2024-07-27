package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlin.test.Test

internal class DefaultActionHandlerContextTest {

    @Test
    fun `sliced action context should update the parent state as expected`() {
        var parentState = NestedState(PlainState())
        val actionContext = DefaultActionContext(
            getState = { parentState },
            setState = { parentState = it },
            sendFunction = { Job() },
        )

        val slicedActionContext = actionContext.slice(
            stateToSlice = { it.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        )

        slicedActionContext.commit(PlainState(value = 42))

        parentState shouldBe NestedState(PlainState(value = 42))
    }
}
