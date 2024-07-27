package org.lettux

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import org.lettux.core.ActionHandler
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.extension.state
import org.lettux.factory.createStore

internal class DefaultStoreTest {

    private val testActionHandler = ActionHandler<PlainState> { action ->
        if (action is HandledAction) {
            commit(state.copy(value = state.value + 1))
        }
    }

    @Test
    fun `should send an action to the store and mutate the state`() = runTest {
        val store = createStore(
            initialState = PlainState(value = 0),
            actionHandler = testActionHandler,
            storeScope = this,
        )

        store.send(HandledAction)

        store.state shouldBe PlainState(value = 1)
    }

    @Test
    fun `middlewares should intercept the action once`() = runTest {
        var counter = 0
        val first = Middleware { action, _, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, _, chain ->
            counter++
            chain.proceed(action)
        }
        val store = createStore(
            initialState = PlainState(),
            actionHandler = testActionHandler,
            middlewares = listOf(first, second),
            storeScope = this,
        )

        store.send(HandledAction)

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
        val store = createStore(
            initialState = PlainState(),
            actionHandler = testActionHandler,
            middlewares = listOf(first, second),
            storeScope = this,
        )

        store.send(HandledAction)

        callOrder shouldBe listOf(1, 2)
    }

    @Test
    fun `a middleware should receive a mutated outcome when the state changed`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, _, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val store = createStore(
            initialState = PlainState(),
            actionHandler = testActionHandler,
            middlewares = listOf(middleware),
            storeScope = this,
        )

        store.send(HandledAction).join()

        outcome shouldBe Outcome.StateMutated(PlainState(value = 1))
    }

    @Test
    fun `a middleware should receive a no-mutation outcome when the state hasn't changed`() =
        runTest {
            lateinit var outcome: Outcome
            val middleware = Middleware { action, _, chain ->
                chain.proceed(action).also { outcome = it }
            }
            val store = createStore(
                initialState = PlainState(),
                actionHandler = testActionHandler,
                middlewares = listOf(middleware),
                storeScope = this,
            )

            store.send(UnHandledAction).join()

            outcome shouldBe Outcome.NoMutation
        }
}
