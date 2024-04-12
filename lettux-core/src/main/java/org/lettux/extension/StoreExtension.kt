package org.lettux.extension

import org.lettux.core.State
import org.lettux.core.Store

val <STATE : State> Store<STATE>.state: STATE get() = states.value
