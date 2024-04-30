package org.lettux.android.logger

import android.util.Log
import org.lettux.core.Action
import org.lettux.core.Chain
import org.lettux.core.Middleware
import org.lettux.core.Outcome
import org.lettux.core.State
import java.util.concurrent.atomic.AtomicInteger

class LoggerMiddleware(private val logger: LogsWriter = AndroidLogsWriter) : Middleware {

    private val indentationCounter = AtomicInteger(0)

    override suspend fun intercept(action: Action, state: State, chain: Chain): Outcome {
        val localIndentation = (0 until indentationCounter.get()).joinToString(separator = "") { "\t|" }

        indentationCounter.incrementAndGet()

        logger.writeLog("$localIndentation ⇨ $action")
        val outcome = chain.proceed(action)

        indentationCounter.decrementAndGet()

        when (outcome) {
            is Outcome.StateMutated -> {
                logger.writeLog("$localIndentation \tState: ${outcome.state}")
            }

            Outcome.NoMutation -> {
                logger.writeLog("$localIndentation No state mutation")
            }
        }

        logger.writeLog("$localIndentation ⇦ $action")
        logger.writeLog("\n")

        return outcome
    }
}