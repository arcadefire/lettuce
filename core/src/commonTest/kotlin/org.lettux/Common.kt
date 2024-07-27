package org.lettux

import org.lettux.core.Action
import org.lettux.core.State

object HandledAction : Action
object UnHandledAction : Action

data class NestedState(val innerState: PlainState = PlainState()) : State
data class PlainState(val value: Int = 0) : State
