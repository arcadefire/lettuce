package org.lettux.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

internal fun CoroutineScope.defaultLaunch(
    block: suspend CoroutineScope.() -> Unit
) = launch(start = CoroutineStart.UNDISPATCHED) { block() }