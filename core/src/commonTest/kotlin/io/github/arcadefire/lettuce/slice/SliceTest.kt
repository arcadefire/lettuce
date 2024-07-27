package io.github.arcadefire.lettuce.slice

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import io.github.arcadefire.lettuce.HandledAction
import io.github.arcadefire.lettuce.NestedState
import io.github.arcadefire.lettuce.PlainState
import io.github.arcadefire.lettuce.UnHandledAction
import io.github.arcadefire.lettuce.core.ActionHandler
import io.github.arcadefire.lettuce.core.Middleware
import io.github.arcadefire.lettuce.core.Outcome
import io.github.arcadefire.lettuce.core.Store
import io.github.arcadefire.lettuce.core.Subscription
import io.github.arcadefire.lettuce.extension.state
import io.github.arcadefire.lettuce.factory.sliceStore
import io.github.arcadefire.lettuce.factory.createStore

internal class SliceTest {

    private val testActionHandler = ActionHandler<NestedState> { action ->
        if (action is HandledAction) {
            commit(
                state.copy(
                    innerState = state.innerState.copy(value = state.innerState.value + 1)
                )
            )
        }
    }

    private fun testStore(storeScope: CoroutineScope) = createStore(
        initialState = NestedState(),
        actionHandler = testActionHandler,
        middlewares = emptyList(),
        subscription = null,
        storeScope = storeScope,
    )

    @Test
    fun `should slice from the parent store`() = runTest {
        val sliced: Store<PlainState> = sliceStore(
            store = testStore(storeScope = this),
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        )

        sliced.send(HandledAction)

        sliced.state shouldBe PlainState(value = 1)
    }

    @Test
    fun `slice middlewares should intercept the action once`() = runTest {
        var counter = 0
        val first = Middleware { action, _, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, _, chain ->
            counter++
            chain.proceed(action)
        }

        val sliced: Store<PlainState> = sliceStore(
            store = testStore(storeScope = this),
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(first, second),
        )

        sliced.send(HandledAction)

        counter shouldBe 2
    }

    @Test
    fun `middlewares should be executed in the order they are provided`() = runTest {
        val callOrder = mutableListOf<Int>()
        val first = Middleware { action, _, chain ->
            callOrder.add(1)
            chain.proceed(action)
        }
        val second = Middleware { action, _, chain ->
            callOrder.add(2)
            chain.proceed(action)
        }
        val sliced: Store<PlainState> = sliceStore(
            store = testStore(storeScope = this),
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(first, second),
        )

        sliced.send(HandledAction)

        callOrder shouldBe listOf(1, 2)
    }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state changes`() =
        runTest {
            lateinit var outcome: Outcome
            val middleware = Middleware { action, _, chain ->
                chain.proceed(action).also { outcome = it }
            }
            val sliced: Store<PlainState> = sliceStore(
                store = testStore(storeScope = this),
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            )

            sliced.send(HandledAction)

            outcome shouldBe Outcome.StateMutated(PlainState(value = 1))
        }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state doesn't change`() =
        runTest {
            lateinit var outcome: Outcome
            val middleware = Middleware { action, _, chain ->
                chain.proceed(action).also { outcome = it }
            }
            val sliced: Store<PlainState> = sliceStore(
                store = testStore(storeScope = this),
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            )

            sliced.send(UnHandledAction)

            outcome shouldBe Outcome.NoMutation
        }

    @Test
    fun `slice subscription should receive the expected sliced state`() =
        runTest {
            lateinit var subscribedState: PlainState
            val subscription = Subscription { states ->
                states
                    .onEach { subscribedState = it }
                    .map { UnHandledAction }
                    .take(1)
            }
            val sliced: Store<PlainState> = sliceStore(
                store = testStore(storeScope = this),
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                subscription = subscription,
            )

            sliced.send(HandledAction)

            advanceUntilIdle()

            subscribedState shouldBe PlainState(value = 1)
        }
}
