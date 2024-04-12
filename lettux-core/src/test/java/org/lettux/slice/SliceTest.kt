package org.lettux.slice

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.core.Action
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.Store
import org.lettux.core.state
import org.lettux.factory.sliceStoreFactory
import org.lettux.factory.storeFactory

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

    private val storeFactory = storeFactory(
        initialState = ParentState(),
        actionHandler = testActionHandler,
    )

    @Test
    fun `should slice from the parent store`() = runTest {
        val sliced: Store<InnerState> = sliceStoreFactory(
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) }
        ).get(this)

        sliced.send(HandledAction)

        sliced.state shouldBe InnerState(value = 1)
    }

    @Test
    fun `slice middlewares should intercept the action once`() = runTest {
        var counter = 0
        val first = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }

        val sliced: Store<InnerState> = sliceStoreFactory(
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(first, second),
        ).get(this)

        sliced.send(HandledAction)

        counter shouldBe 2
    }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state changes`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val sliced: Store<InnerState> = sliceStoreFactory(
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(middleware),
        ).get(this)

        sliced.send(HandledAction)

        outcome shouldBe Outcome.StateMutated(InnerState(value = 1))
    }

    @Test
    fun `slice middleware should receive the expected outcome when the parent state doesn't change`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val sliced: Store<InnerState> = sliceStoreFactory(
            storeFactory = storeFactory,
            stateToSlice = { state -> state.innerState },
            sliceToState = { state, slice -> state.copy(innerState = slice) },
            middlewares = listOf(middleware),
        ).get(this)

        sliced.send(UnHandledAction)

        outcome shouldBe Outcome.NoMutation
    }
}
