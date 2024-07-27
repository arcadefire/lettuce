package org.lettuce.android.logger

import android.util.Log
import org.lettuce.core.Action
import org.lettuce.core.Chain
import org.lettuce.core.Middleware
import org.lettuce.core.Outcome
import org.lettuce.core.State
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