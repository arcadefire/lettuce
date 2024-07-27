package org.lettuce

import org.lettuce.core.Action
import org.lettuce.core.State

object HandledAction : Action
object UnHandledAction : Action

data class NestedState(val innerState: PlainState = PlainState()) : State
data class PlainState(val value: Int = 0) : State
