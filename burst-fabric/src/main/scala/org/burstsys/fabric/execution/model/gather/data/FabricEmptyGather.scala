/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.gather.data

import org.burstsys.fabric.execution.model.scanner.FabricScanner

/**
 * Generic gather used to return a no data state (empty scan)
 */
trait FabricEmptyGather extends FabricDataGather

object FabricEmptyGather {

  def apply(scanner: FabricScanner): FabricEmptyGather = FabricEmptyGatherContext().initialize(scanner)

}

private[fabric] final case
class FabricEmptyGatherContext() extends FabricDataGatherContext with FabricEmptyGather {

  override def resultMessage: String = "EMPTY"

  override def rowCount: Long = 0

  override def overflowCount: Long = 0

  override def limitCount: Long = 0

  override def queryCount: Long = 0

  override def successCount: Long = 0

}
