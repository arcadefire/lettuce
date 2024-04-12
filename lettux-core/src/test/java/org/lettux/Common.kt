package org.lettux

import org.lettux.core.Action

object HandledAction : Action
object UnHandledAction : Action

data class TestState(val value: Int = 0)
data class InnerState(val value: Int = 0)
data class ParentState(val innerState: InnerState = InnerState())