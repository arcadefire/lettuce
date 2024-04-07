package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestScope
import org.lettux.core.*
import org.junit.jupiter.api.Test

internal class SliceTest {

    private object HandledAction : Action
    private object UnHandledAction : Action

    private data class InnerState(val value: Int = 0)
    private data class ParentState(val innerState: InnerState = InnerState())

    private val testActionHandler = ActionHandler<ParentState> {
        if (action is HandledAction) {
            commit(
                state.copy(
                    innerState = state.innerState.copy(value = state.innerState.value + 1)
                )
            )
        }
    }

    private val store = createStore(
        initialState = ParentState(),
        actionHandler = testActionHandler,
        storeScope = TestScope(),
    )

    @Test
    fun `should slice from the parent store`() {
        val sliced = store
            .slice(
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) }
            )

        sliced.send(HandledAction)

        sliced.state shouldBe InnerState(value = 1)
    }

    @Test
    fun `slice middlewares should intercept the action once`() {
        var counter = 0
        val first = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val sliced = store
            .slice(
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(first, second),
            )

        sliced.send(HandledAction)

        counter shouldBe 2
    }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state changes`() {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val sliced = store
            .slice(
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            )

        sliced.send(HandledAction)

        outcome shouldBe Outcome.StateMutated(InnerState(value = 1))
    }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state doesn't change`() {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val sliced = store
            .slice(
                stateToSlice = { state -> state.innerState },
                sliceToState = { state, slice -> state.copy(innerState = slice) },
                middlewares = listOf(middleware),
            )

        sliced.send(UnHandledAction)

        outcome shouldBe Outcome.NoMutation
    }
}