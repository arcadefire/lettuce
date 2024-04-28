package org.lettux.core

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.Test
import org.lettux.HandledAction
import org.lettux.PlainState
import org.lettux.UnHandledAction
import org.lettux.extension.combine
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

    @Test
    fun `should combine two subscriptions in a single one`() {
        val testScope = TestScope()
        val callOrder = ArrayList<Int>(2)
        val first = Subscription<PlainState> { states ->
            states.map {
                callOrder.add(1)
                UnHandledAction
            }
        }
        val second = Subscription<PlainState> { states ->
            states.map {
                callOrder.add(2)
                UnHandledAction
            }
        }
        val store = storeFactory(
            initialState = PlainState(value = 0),
            actionHandler = testActionHandler,
            subscription = combine(first, second)
        ).get(storeScope = testScope)

        store.send(UnHandledAction)

        testScope.advanceUntilIdle()
        testScope.cancel()

        callOrder shouldBe listOf(1, 2)
    }

    @Test
    fun `combine with a single subscriber just returns the subscriber`() {
        val testScope = TestScope()
        var counter = 0
        val first = Subscription<PlainState> { states ->
            states.map {
                counter++
                UnHandledAction
            }
        }
        val store = storeFactory(
            initialState = PlainState(value = 0),
            actionHandler = testActionHandler,
            subscription = combine(first)
        ).get(storeScope = testScope)

        store.send(UnHandledAction)

        testScope.advanceUntilIdle()
        testScope.cancel()

        counter shouldBe 1
    }
}
