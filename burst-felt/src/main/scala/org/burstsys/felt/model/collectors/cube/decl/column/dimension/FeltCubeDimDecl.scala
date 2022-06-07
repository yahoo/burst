/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeColSem
import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColDecl

/**
 *
 */
trait FeltCubeDimDecl extends AnyRef with FeltCubeColDecl[FeltCubeColSem] {

  def semantic: FeltCubeDimColSem

  def semanticType: FeltDimSemType

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def reduceStatics: FeltCubeDimDecl = this

}
