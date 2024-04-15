package org.lettux.core

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.HandledAction
import org.lettux.PlainState
import org.lettux.UnHandledAction
import org.lettux.extension.state
import org.lettux.factory.storeFactory

internal class SubscriptionTest {

    private val testActionHandler = ActionHandler<PlainState> {
        if (action is HandledAction) {
            commit(state.copy(value = state.value + 1))
        }
    }

    @Test
    fun `should subscribe to state changes`() {
        val collectedStates = ArrayList<PlainState>(2)
        val testScope = TestScope()
        val store = storeFactory(
            initialState = PlainState(value = 0),
            actionHandler = testActionHandler,
            subscription = { states ->
                states
                    .onEach {
                        collectedStates.add(it)
                    }
                    .map {
                        it.value.takeIf { it == 1 }?.let { HandledAction } ?: UnHandledAction
                    }
            }
        ).get(storeScope = testScope)

        store.send(HandledAction)

        testScope.advanceUntilIdle()
        testScope.cancel()

        collectedStates shouldBe listOf(PlainState(value = 1), PlainState(value = 2))
    }
}