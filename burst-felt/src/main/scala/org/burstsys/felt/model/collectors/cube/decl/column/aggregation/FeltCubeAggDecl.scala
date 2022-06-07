/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

import org.burstsys.felt.model.collectors.cube.decl.column.FeltCubeColDecl

/**
 * A FELT Cube Aggregation Column
 */
trait FeltCubeAggDecl extends FeltCubeColDecl[FeltCubeAggColSem] {

  def semantic: FeltCubeAggColSem

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def reduceStatics: FeltCubeAggDecl = this

}
