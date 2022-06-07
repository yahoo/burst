/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

package object coerce {

  private[felt] final val CoerceName = "coerce"

  abstract
  class FeltCubeDimCoerceSem extends FeltCubeDimColSem {
    final lazy val semanticRt: FeltCubeDimCoerceSemRt = FeltCubeDimCoerceSemRt(bType)
  }

  object COERCE_DIMENSION_SEMANTIC extends FeltDimSemType(CoerceName)

}
