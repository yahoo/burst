/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

/**
 * A variant of [[FeltGenSplice]] that takes more than one [[FeltSpliceGenerator]]
 * and combines them together
 */
trait FeltMultiGenSplice extends FeltSplice {

  /**
   * the provided splice generator
   *
   * @return
   */
  def generators: Array[FeltSpliceGenerator]

}

object FeltMultiGenSplice {
  def apply(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
            placement: FeltPlacement, generators: Array[FeltSpliceGenerator], ordinal: Int = 0): FeltMultiGenSplice =
    FeltMultiGenSpliceContext(global = global: FeltGlobal, location = location, spliceName = spliceName: String,
      pathName = pathName: BrioPathName,
      placement = placement: FeltPlacement, generators = generators, ordinal = ordinal: Int)
}

private final case
class FeltMultiGenSpliceContext(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
                                placement: FeltPlacement, generators: Array[FeltSpliceGenerator], ordinal: Int)
  extends FeltSpliceContext(
    global = global, location = location, spliceName = spliceName, pathName = pathName, placement = placement
  ) with FeltMultiGenSplice {

  feltType = FeltType.unit

  override protected
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    generators.flatMap(_ (cursor indentRight)).mkString
  }

}
