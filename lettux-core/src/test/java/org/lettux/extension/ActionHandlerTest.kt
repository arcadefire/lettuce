package org.lettux.extension

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.HandledAction
import org.lettux.InnerState
import org.lettux.ParentState
import org.lettux.core.Action
import org.lettux.core.ActionHandler
import org.lettux.core.state
import org.lettux.factory.storeFactory

internal class ActionHandlerTest {

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
            ).get(storeScope = this)

            store.send(HandledAction)

            store.state shouldBe ParentState(InnerState(1))
        }
    }

    @Test
    fun `should send an action to the same store from within the handle function`() {
        runTest {
            val actionHandler = ActionHandler<ParentState> {
                when (action) {
                    FirstAction -> send(SecondAction)
                    SecondAction -> {
                        commit(state.copy(innerState = state.innerState.copy(value = 1)))
                    }
                }
            }
            val store = storeFactory(
                initialState = ParentState(InnerState()),
                actionHandler = actionHandler
            ).get(storeScope = this)

            store.send(FirstAction)

            advanceUntilIdle()

            store.state shouldBe ParentState(InnerState(1))
        }
    }

    private data object FirstAction : Action
    private data object SecondAction : Action
}