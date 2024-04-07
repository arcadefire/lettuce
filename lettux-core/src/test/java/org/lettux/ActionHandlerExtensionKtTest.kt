package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.lettux.core.Action
import org.lettux.core.ActionHandler
import org.lettux.core.state

internal class ActionHandlerExtensionKtTest {

    private data object TestAction : Action

    private data class InnerState(val value: Int = 0)
    private data class ParentState(val innerState: InnerState = InnerState())

    @Test
    fun `pullback should transform an action handler of a sub-state into an action handler of the parent state`() {
        val sliceActionHandler = ActionHandler<InnerState> {
            commit(state.copy(value = state.value + 1))
        }
        val store = createStore(
            initialState = ParentState(InnerState(0)),
            actionHandler = sliceActionHandler.pullback(
                stateToSlice = { it.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) }
            ),
            storeScope = TestScope(),
        )

        store.send(TestAction)

        store.state shouldBe ParentState(InnerState(1))
    }
}