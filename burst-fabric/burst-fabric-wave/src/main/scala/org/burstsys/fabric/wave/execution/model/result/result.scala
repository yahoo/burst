/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging._

package object result extends VitalsLogger {

  /**
   * tracking of results in various scan artifacts
   */
  trait FabricResult extends Any {

    /**
     * the status associated with a scan artifact outcome
     */
    def resultStatus: FabricResultStatus

    /**
     * the helpful messages associated with a scan artifact outcome if needed
     */
    def resultMessage: String

    /**
     * was this result a success?
     *
     * @return
     */
    def succeeded: Boolean = resultStatus.isSuccess
  }
}
