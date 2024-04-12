package org.lettux.extension

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.HandledAction
import org.lettux.InnerState
import org.lettux.ParentState
import org.lettux.TestState
import org.lettux.core.ActionHandler
import org.lettux.core.state
import org.lettux.storeFactory

internal class ActionHandlerExtensionKtTest {

    @Test
    fun `pullback should transform an action handler of a sub-state into an action handler of the parent state`() {
        runTest {
            val sliceActionHandler = ActionHandler<InnerState> {
                commit(state.copy(value = state.value + 1))
            }
            val store = storeFactory<ParentState>(
                initialState = ParentState(InnerState()),
                actionHandler = sliceActionHandler.pullback(
                    stateToSlice = { it.innerState },
                    sliceToState = { state, slice -> state.copy(innerState = slice) }
                ),
            ).create(storeScope = this)

            store.send(HandledAction)

            store.state shouldBe ParentState(InnerState(1))
        }
    }
}