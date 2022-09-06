/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimColSem, FeltDimSemType}

package object `enum` {

  private[felt] final val EnumName = "enum"

  object ENUM_DIMENSION_SEMANTIC extends FeltDimSemType(EnumName)

  abstract
  class FeltCubeDimEnumSem extends FeltCubeDimColSem {
    override
    val semanticRt: FeltCubeDimEnumSemRt
  }

}
