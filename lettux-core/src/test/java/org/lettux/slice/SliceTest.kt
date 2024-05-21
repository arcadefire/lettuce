package org.lettux.slice

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.HandledAction
import org.lettux.NestedState
import org.lettux.PlainState
import org.lettux.UnHandledAction
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.Store
import org.lettux.core.Subscription
import org.lettux.extension.state
import org.lettux.factory.sliceStore
import org.lettux.factory.createStore

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

    private val storeFactory = createStore(
        initialState = NestedState(),
        actionHandler = testActionHandler,
    )

    @Test
    fun `should slice from the parent store`() = runTest {
        val sliced: Store<PlainState> = sliceStore(
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        ).get(this)

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
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(first, second),
        ).get(this)

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
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(first, second),
        ).get(this)

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
                storeFactory = storeFactory,
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            ).get(this)

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
                storeFactory = storeFactory,
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            ).get(this)

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
                storeFactory = storeFactory,
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                subscription = subscription,
            ).get(this)

            sliced.send(HandledAction)

            advanceUntilIdle()

            subscribedState shouldBe PlainState(value = 1)
        }
}
