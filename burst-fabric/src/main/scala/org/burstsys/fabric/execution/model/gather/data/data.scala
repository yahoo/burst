/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.gather

import org.burstsys.vitals.logging.VitalsLogger

package object data extends VitalsLogger {

  /**
   * gather for unit tests...
   */
  final case
  class MockDataGather() extends FabricDataGatherContext {

    val resultMessage: String = "MOCK_GATHER"

    override def rowCount: Long = 0

    override def overflowCount: Long = 0

    override def limitCount: Long = 0

    override def queryCount: Long = 0

    override def successCount: Long = 0

  }

}
