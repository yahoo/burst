/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result

package object state {
  /**
   *
   * @param code
   * @param damped
   */
  sealed case
  class FabricScanState(code: Int, damped: Boolean = false) {
    override def toString: String = getClass.getSimpleName.stripSuffix("$")
  }

  /**
   * Scan started but not finished yet
   */
  object FabricScanRunning extends FabricScanState(1)

  /**
   * Normal scan return
   */
  object FabricSuccessStatus extends FabricScanState(2)

  /**
   * Scan failed due to server internal error. No data is returned.
   */
  object FabricExceptionStatus extends FabricScanState(3, true)

  /**
   * Scan Failed due to an error in the query syntax or semantics. No data is returned.
   */
  object FabricInvalidStatus extends FabricScanState(5, true)

  /**
   * Scan Failed due to timeout.
   */
  object FabricTimeoutStatus extends FabricScanState(6, true)

  /**
   * Scan failed due to server internal error. No data is returned.
   */
  object FabricNotReadyStatus extends FabricScanState(7, true)

  /**
   * Scan failed due to Fabric store error.
   */
  object FabricStoreErrorStatus extends FabricScanState(8, true)

  /**
   * Scan failed due to view error.
   */
  object FabricViewErrorStatus extends FabricScanState(9, true)

  /**
   * Scan failed due to empty dataset.
   */
  object FabricNoDataStatus extends FabricScanState(10, true)
}
