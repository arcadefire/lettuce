package org.lettux

import org.lettux.core.Action

object HandledAction : Action
object UnHandledAction : Action

data class NestedState(val innerState: PlainState = PlainState())
data class PlainState(val value: Int = 0)
