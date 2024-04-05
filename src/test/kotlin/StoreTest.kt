import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.lettux.core.*
import org.lettux.createStore
import org.junit.jupiter.api.Test

internal class StoreImplTest {

    private object HandledAction : Action
    private object UnHandledAction : Action

    private data class TestState(val value: Int)

    private val testActionHandler = ActionHandler<TestState> {
        if (action is HandledAction) {
            commit(state.copy(value = state.value + 1))
        }
    }

    @Test
    fun `should send an action to the store and mutate the state`() {
        val store = createStore(
            initialState = TestState(value = 0),
            actionHandler = testActionHandler,
            storeScope = TestScope(),
        )

        store.send(HandledAction)

        store.state shouldBe TestState(value = 1)
    }

    @Test
    fun `middlewares should intercept the action once`() {
        var counter = 0
        val first = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val second = Middleware { action, chain ->
            counter++
            chain.proceed(action)
        }
        val store = createStore(
            initialState = TestState(value = 0),
            actionHandler = testActionHandler,
            storeScope = TestScope(),
            middlewares = listOf(first, second),
        )

        store.send(HandledAction)

        counter shouldBe 2
    }

    @Test
    fun `a middleware should receive a mutated outcome when the state changed`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val store = createStore(
            initialState = TestState(value = 0),
            actionHandler = testActionHandler,
            storeScope = TestScope(),
            middlewares = listOf(middleware),
        )

        store.send(HandledAction).join()

        outcome shouldBe Outcome.StateMutated(TestState(value = 1))
    }

    @Test
    fun `a middleware should receive a no-mutation outcome when the state hasn't changed`() = runTest {
        lateinit var outcome: Outcome
        val middleware = Middleware { action, chain ->
            chain.proceed(action).also { outcome = it }
        }
        val store = createStore(
            initialState = TestState(value = 0),
            actionHandler = testActionHandler,
            storeScope = TestScope(),
            middlewares = listOf(middleware),
        )

        store.send(UnHandledAction).join()

        outcome shouldBe Outcome.NoMutation
    }
}