package io.github.arcadefire.lettuce.extension

import io.github.arcadefire.lettuce.core.State
import io.github.arcadefire.lettuce.core.Store

val <STATE : State> Store<STATE>.state: STATE get() = states.value
