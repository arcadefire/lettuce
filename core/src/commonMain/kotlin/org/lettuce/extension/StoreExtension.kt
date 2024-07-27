package org.lettuce.extension

import org.lettuce.core.State
import org.lettuce.core.Store

val <STATE : State> Store<STATE>.state: STATE get() = states.value
