package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.state
import org.lettux.factory.storeFactory

internal class DefaultStoreTest {

    private val testActionHandler = ActionHandler<PlainState> {
        if (action is HandledAction) {
            commit(state.copy(value = state.value + 1))
        }
    }

    @Test
    fun `should send an action to the store and mutate the state`() = runTest {
        val store = storeFactory(
            initialState = PlainState(value = 0),
            actionHandler = testActionHandler,
        ).get(storeScope = this)

        store.send(HandledAction)

        store.state shouldBe PlainState(value = 1)
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
            initialState = PlainState(),
            actionHandler = testActionHandler,
            middlewares = listOf(first, second)
        ).get(storeScope = this)

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
            initialState = PlainState(),
            actionHandler = testActionHandler,
            middlewares = listOf(middleware)
        ).get(storeScope = this)

        store.send(HandledAction).join()

        outcome shouldBe Outcome.StateMutated(PlainState(value = 1))
    }

    @Test
    fun `a middleware should receive a no-mutation outcome when the state hasn't changed`() =
        runTest {
            lateinit var outcome: Outcome
            val middleware = Middleware { action, chain ->
                chain.proceed(action).also { outcome = it }
            }
            val store = storeFactory(
                initialState = PlainState(),
                actionHandler = testActionHandler,
                middlewares = listOf(middleware)
            ).get(storeScope = this)

            store.send(UnHandledAction).join()

            outcome shouldBe Outcome.NoMutation
        }
}
