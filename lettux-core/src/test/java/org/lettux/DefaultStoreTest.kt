package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.lettux.core.*
import org.junit.jupiter.api.Test

internal class DefaultStoreTest {

    private val testActionHandler = ActionHandler<TestState> {
        if (action is HandledAction) {
            commit(state.copy(value = state.value + 1))
        }
    }

    @Test
    fun `should send an action to the store and mutate the state`() = runTest {
        val store = storeFactory(
            initialState = TestState(value = 0),
            actionHandler = testActionHandler,
        ).create(storeScope = this)

        store.send(HandledAction)

        store.state shouldBe TestState(value = 1)
    }

    @Test
    fun `middlewares should intercept the action once`() = runTest {
        var counter = 0
        val first = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val store = storeFactory(
            initialState = TestState(),
            actionHandler = testActionHandler,
            middlewares = listOf(first, second)
        ).create(storeScope = this)

        store.send(HandledAction)

        counter shouldBe 2
    }

    @Test
    fun `a middleware should receive a mutated outcome when the state changed`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val store = storeFactory(
            initialState = TestState(),
            actionHandler = testActionHandler,
            middlewares = listOf(middleware)
        ).create(storeScope = this)

        store.send(HandledAction).join()

        outcome shouldBe Outcome.StateMutated(TestState(value = 1))
    }

    @Test
    fun `a middleware should receive a no-mutation outcome when the state hasn't changed`() =
        runTest {
            lateinit var outcome: Outcome
            val middleware = Middleware { action, chain ->
                chain.proceed(action).also { outcome = it }
            }
            val store = storeFactory(
                initialState = TestState(),
                actionHandler = testActionHandler,
                middlewares = listOf(middleware)
            ).create(storeScope = this)

            store.send(UnHandledAction).join()

            outcome shouldBe Outcome.NoMutation
        }
}