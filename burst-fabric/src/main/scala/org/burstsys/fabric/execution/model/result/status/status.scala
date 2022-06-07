/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.result

package object status {

  sealed case class FabricResultStatus(name: String, isSuccess: Boolean, isComplete: Boolean = true) {
    override def toString: String = s"$name, success=$isSuccess"
    final def isFailure:Boolean = !isSuccess
  }

  object FabricInProgressResultStatus extends FabricResultStatus("IN_PROGRESS", false, false)

  object FabricUnknownResultStatus extends FabricResultStatus("UNKNOWN", false)

  object FabricSuccessResultStatus extends FabricResultStatus("SUCCESS", true)

  object FabricFaultResultStatus extends FabricResultStatus("EXCEPTION", false)

  object FabricInvalidResultStatus extends FabricResultStatus("INVALID", false)

  object FabricTimeoutResultStatus extends FabricResultStatus("TIMEOUT", false)

  object FabricNotReadyResultStatus extends FabricResultStatus("NOT_READY", false)

  object FabricNoDataResultStatus extends FabricResultStatus("NO_DATA", true)

  object FabricStoreErrorResultStatus extends FabricResultStatus("STORE_ERROR", false)


}
