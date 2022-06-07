/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.lattice.traveler._
import org.burstsys.felt.model.schema.traveler.FeltTravelerGenerator
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * lattice (brio object tree traversal) code generation for traveler
 */
trait FeltLatTravGen {

  /**
   *
   * @param treeNode
   * @param cursor
   * @return
   */
  def generateLatticeSchemaRoot(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltLatTravGen {

  def apply(generator:FeltTravelerGenerator): FeltLatTravGen =
    FeltLatTravGenContext(generator)

}

private[lattice] final case
class FeltLatTravGenContext(generator:FeltTravelerGenerator) extends FeltLatTravGen
  with FeltLatRefScalRelTravGen with FeltLatRefScalTravGen with FeltLatValVecTravGen with FeltLatValMapTravGen
  with FeltLatRefVecTravGen with FeltLatRefScalTunTravGen {

  override def generateLatticeSchemaRoot(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode =
    processRefScal(treeNode)

}
