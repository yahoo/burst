/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take

import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.tree.source.S

/**
 *
 * @param code
 */
case class FeltCubeTakeSemMode(code: Int) {

  ///////////////////////////////////////////////////////////////////////////////////
  // code generation
  ///////////////////////////////////////////////////////////////////////////////////

  def generateCode(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeTakeSemMode].getName}($code)"

  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"$S${classOf[FeltCubeTakeSemMode].getName}($code)"

}
