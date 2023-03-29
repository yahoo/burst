/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.burnin

import org.burstsys.fabric.wave.execution.model.result.{FabricExecuteResult, status}

object BurnInRunBatchStats {
  def apply(result: FabricExecuteResult): BurnInRunBatchStats = {
    val runStatus = result.resultStatus match {
      case status.FabricInProgressResultStatus
           | status.FabricUnknownResultStatus
           | status.FabricFaultResultStatus
           | status.FabricInvalidResultStatus
           | status.FabricTimeoutResultStatus
           | status.FabricNotReadyResultStatus
           | status.FabricStoreErrorResultStatus =>
        BatchLimited

      case status.FabricNoDataResultStatus | status.FabricSuccessResultStatus =>
        BatchCompletedNormally

      case _ => ???
    }

    BurnInRunBatchStats(runStatus)
  }
}

case class BurnInRunBatchStats(
                                status: BurnInRunBatchStatus,
                              // bytes
                              // items
                              // observed time
                              // min/avg/max load time
                              // min/avg/max scan time
                              // global scan bytes/s
                              // min/avg/max scan bytes/s
                              // # cold loaded workers
                              // # warm loaded workers
                              //
                              ) {

  def merge(other: BurnInRunBatchStats): BurnInRunBatchStats = {
    val mergedStatus = (status, other.status) match {
      case (BatchDidNotRun, BatchDidNotRun)  => BatchDidNotRun
      case (BatchDidNotRun, _)  => BatchLimited
      case (BatchLimited, _)  => BatchLimited
      case (BatchCompletedNormally, BatchCompletedNormally)  => BatchCompletedNormally
      case (BatchCompletedNormally, _)  => BatchLimited
      case other =>
        log warn(s"Unexpected batch status combination $other")
        BatchLimited
    }

    BurnInRunBatchStats(mergedStatus)
  }
}


sealed trait BurnInRunBatchStatus

/** All queries/datasets completed as expected */
case object BatchCompletedNormally extends BurnInRunBatchStatus

/** Only some queries/datasets completed as expected */
case object BatchLimited extends BurnInRunBatchStatus

/** No queries/datasets were run */
case object BatchDidNotRun extends BurnInRunBatchStatus
