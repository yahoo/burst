/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.supervisor

import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.scatter.TeslaScatterRequestContext
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}

package object op extends VitalsLogger {

  /**
   * base type for all cache requests
   *
   * @tparam R the return type of the operation
   */
  abstract class CacheOpRequest[R] extends TeslaScatterRequestContext[R] {

    def ruid: VitalsUid = newBurstUid

    /**
     * the target worker for this request
     */
    def worker: FabricWorkerNode

    /**
     * the supervisor data service
     */
    def data: FabricSupervisorData

  }

}
