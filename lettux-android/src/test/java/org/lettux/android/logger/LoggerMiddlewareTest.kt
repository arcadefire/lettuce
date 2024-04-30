package org.lettux.android.logger

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.test.runTest

import org.junit.Test
import org.lettux.core.Action
import org.lettux.core.Chain
import org.lettux.core.Outcome
import org.lettux.core.State

class LoggerMiddlewareTest {

    private data class AnyState(val value: Int = 0) : State
    private data object AnyAction : Action

    private val logsWriter = mockk<LogsWriter>(relaxed = true)
    private val loggerMiddleware: LoggerMiddleware = LoggerMiddleware(logsWriter)
    private val action: Action = AnyAction
    private val state: State = AnyState()
    private val mockChain: Chain = mockk()

    @Test
    fun `should log when the state changes`() = runTest {
        val outcome = Outcome.StateMutated(state)
        coEvery { mockChain.proceed(action) } returns outcome

        loggerMiddleware.intercept(action, state, mockChain)

        coVerify { mockChain.proceed(action) }
        verifyLog(" ⇨ $action")
        verifyLog(" \tState: $state")
        verifyLog(" ⇦ $action")
        verifyLog("\n")
    }

    @Test
    fun `should log when the state doesn't change`() = runTest {
        val outcome = Outcome.NoMutation
        coEvery { mockChain.proceed(action) } returns outcome

        loggerMiddleware.intercept(action, state, mockChain)

        coVerify { mockChain.proceed(action) }
        verifyLog(" ⇨ $action")
        verifyLog(" No state mutation")
        verifyLog(" ⇦ $action")
    }

    private fun verifyLog(expectedMessage: String) {
        verify { logsWriter.writeLog(expectedMessage) }
    }
}