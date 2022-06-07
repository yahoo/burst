/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.websocket

object ExecutionState {

  final val unknown: ExecutionState = ExecutionStateUnknown()
  final val notStarted: ExecutionState = ExecutionStateNotStarted()
  final val inProgress: ExecutionState = ExecutionStateInProgress()
  final val succeeded: ExecutionState = ExecutionStateSucceeded()
  final val failed: ExecutionState = ExecutionStateFailed()
  final val tardy: ExecutionState = ExecutionStateTardy()
  final val cancelled: ExecutionState = ExecutionStateCancelled()

  sealed class ExecutionState(state: String, val complete: Boolean = false) {
    def msg: String = state
  }

  private case class ExecutionStateUnknown() extends ExecutionState("UNKNOWN")

  private case class ExecutionStateNotStarted() extends ExecutionState("PENDING")

  private case class ExecutionStateInProgress() extends ExecutionState("IN_PROGRESS")

  private case class ExecutionStateTardy() extends ExecutionState("LATE")

  private case class ExecutionStateSucceeded() extends ExecutionState("SUCCEEDED", complete = true)

  private case class ExecutionStateFailed() extends ExecutionState("FAILED", complete = true)

  private case class ExecutionStateCancelled() extends ExecutionState("CANCELLED", complete = true)

}
