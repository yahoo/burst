/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

package object verbatim {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // basic dimension/functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val VerbatimName = "verbatim"

  abstract
  class FeltCubeDimVerbatimSem extends FeltCubeDimColSem {
    final lazy val semanticRt: FeltCubeDimVerbatimSemRt = FeltCubeDimVerbatimSemRt(bType)

  }

  object VERBATIM_DIMENSION_SEMANTIC extends FeltDimSemType(VerbatimName)

}
