/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.felt.model.types.FeltType

/**
 * This supports splicing generated code that is not found in a [[FeltExpression]]
 * but rather is created during tree processing to support
 * internal code fragments.
 */
trait FeltGenSplice extends FeltSplice {

  /**
   * the provided splice generator
   *
   * @return
   */
  def generator: FeltSpliceGenerator

}

object FeltGenSplice {
  def apply(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
            placement: FeltPlacement, generator: FeltSpliceGenerator, ordinal: Int = 0): FeltGenSplice =
    FeltGenSpliceContext(global = global: FeltGlobal, location = location, spliceName = spliceName: String,
      pathName = pathName: BrioPathName,
      placement = placement: FeltPlacement, generator = generator: FeltSpliceGenerator, ordinal = ordinal: Int)
}

private final case
class FeltGenSpliceContext(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
                           placement: FeltPlacement, generator: FeltSpliceGenerator, ordinal: Int)
  extends FeltSpliceContext(
    global = global, location = location, spliceName = spliceName, pathName = pathName, placement = placement
  ) with FeltGenSplice {

  feltType = FeltType.unit

  override protected
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    generator(cursor.modify(indent = cursor.indent))
  }

}
